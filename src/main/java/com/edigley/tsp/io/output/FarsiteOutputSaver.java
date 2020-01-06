package com.edigley.tsp.io.output;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Font;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.fitness.GoodnessOfFitEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileReader;

public class FarsiteOutputSaver {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteOutputSaver.class);

	public static void saveAsJPG(File perimeterFile, File predictionFile, File layerExtentFile)
			throws IOException, NoSuchAuthorityCodeException, FactoryException {
		// Step 1: Create map
		MapContent map = new MapContent();
		map.setTitle("Farsite Scenario");

		DataStore dataStore1 = ShapeFileReader.getDataStore(perimeterFile);
		DataStore dataStore2 = ShapeFileReader.getDataStore(predictionFile);
		DataStore dataStore3 = ShapeFileReader.getDataStore(layerExtentFile);

		Style style = SLD.createPointStyle("square", Color.red, Color.red, 1.0f, 8.0f);
		StyleBuilder styleBuilder = new StyleBuilder();
		String attributeName = "MINX";
		Font font = styleBuilder.createFont("Times New Roman", 10.0);
		TextSymbolizer textSymb = styleBuilder.createTextSymbolizer(Color.black, font, attributeName);
		Rule rule = styleBuilder.createRule(textSymb);
		style.featureTypeStyles().get(0).rules().add(rule);

		try {
			String[] typeNames1 = dataStore1.getTypeNames();
			String typeName1 = typeNames1[0];
			FeatureSource featureSource1 = dataStore1.getFeatureSource(typeName1);
			Layer layer1 = new FeatureLayer(featureSource1, SLD.createLineStyle(Color.BLUE, 2));
			layer1.setTitle(perimeterFile.getName());

			String[] typeNames2 = dataStore2.getTypeNames();
			String typeName2 = typeNames2[0];
			FeatureSource featureSource2 = dataStore2.getFeatureSource(typeName2);
			Layer layer2 = new FeatureLayer(featureSource2, SLD.createLineStyle(Color.RED, 2));
			layer2.setTitle(predictionFile.getName());

			String[] typeNames3 = dataStore3.getTypeNames();
			String typeName3 = typeNames3[0];
			FeatureSource featureSource3 = dataStore3.getFeatureSource(typeName3);
			Layer layer3 = new FeatureLayer(featureSource3, SLD.createLineStyle(Color.BLACK, 1));
			layer3.setTitle(layerExtentFile.getName());

			map.addLayer(layer2);
			map.addLayer(layer1);
			map.addLayer(layer3);

			// Step 2: Set projection
			CoordinateReferenceSystem crs = CRS.decode(ScenarioProperties.CRS);
			MapViewport vp = map.getViewport();
			vp.setCoordinateReferenceSystem(crs);

			Pair<Long, Double> fireEvolution = FarsiteOutputProcessor.getInstance().getFireEvolution(perimeterFile, predictionFile);
			Long simulatedTime = fireEvolution.getLeft();
			Double normalizedSymmetricDifference = fireEvolution.getRight();
			Double weightedPredictionError = FarsiteOutputProcessor.getInstance().calculateWeightedPredictionError(perimeterFile, predictionFile, ScenarioProperties.DEFAULT_EXPECTED_SIMULATED_TIME);
			
			GoodnessOfFitEvaluator gofEvaluator = new GoodnessOfFitEvaluator();
			Double gof = gofEvaluator.evaluate(predictionFile, perimeterFile);
			
			String[] textToImage = { 
				"File: " + predictionFile.getName(), 
				"Simulated Time: " + simulatedTime,
				"Expected Simulated Time: " + ScenarioProperties.DEFAULT_EXPECTED_SIMULATED_TIME,
				"Normalized Symmetric Difference: " + normalizedSymmetricDifference,
				"Goodness of Fitness: " + gof,
				"Fitness: " + weightedPredictionError 
			};

			// Step 3: Save image
			saveAsJPG(map, predictionFile.getName().replace(".shp", ".jpg"), 800, textToImage);
			
			map.dispose();

		} finally {
			dataStore1.dispose();
			dataStore2.dispose();
			dataStore3.dispose();
		}
	}

	public static void saveAsJPG(final MapContent map, final String fileName, final int imageWidth,
			String[] textToImage) throws MalformedURLException, IOException {

		GTRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(map);

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		try {
			mapBounds = map.getMaxBounds();
			System.out.println(mapBounds.getSpan(1));
			System.out.println(mapBounds.getSpan(0));
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			// imageBounds = new Rectangle( 0, 0, imageWidth, (int) Math.round(imageWidth *
			// heightToWidth) );
			imageBounds = new Rectangle(0, 0, (int) Math.round(imageWidth * heightToWidth), imageWidth);

		} catch (Exception e) {
			// failed to access map layers
			throw new RuntimeException(e);
		}

		BufferedImage image = new BufferedImage(imageBounds.width, imageBounds.height, BufferedImage.TYPE_INT_RGB);

		Graphics2D gr = image.createGraphics();
		gr.setPaint(Color.WHITE);
		gr.fill(imageBounds);

		try {
			renderer.paint(gr, imageBounds, mapBounds);
			File fileToSave = new File(fileName);

			addTextToImage(image, textToImage);

			ImageIO.write(image, "jpeg", fileToSave);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static void addTextToImage(BufferedImage theImage, String[] theText)
			throws IOException, MalformedURLException {
		// final = ImageIO.read(new
		// URL("https://www.researchgate.net/profile/Ronny_Vallejos/publication/273362498/figure/fig1/AS:353314869923841@1461248237727/a-Original-image-Lenna-with-a-size-of-512-512-b-Transformation-of-the-original.png"));

		Graphics g = theImage.getGraphics();
		g.setFont(g.getFont().deriveFont(20f));
		g.setColor(Color.BLACK);
		for (int i =0; i < theText.length; i++) {
			g.drawString(theText[i], 500, (i+1) * 100);
		}
		g.dispose();

		// ImageIO.write(theImage, "png", new File("test.png"));
	}
	
}
