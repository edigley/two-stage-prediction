package com.edigley.tsp.io.output;

import java.awt.Color;
import java.awt.Font;
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
//import org.geotools.styling.Font;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.AdjustedGoodnessOfFit;
import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.vividsolutions.jts.geom.TopologyException;

public class FarsiteOutputSaver {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteOutputSaver.class);

	@SuppressWarnings("rawtypes")
	public static File saveAsJPG(File perimeterFile, File predictionFile, File landscapeExtentFile, ScenarioProperties scenarioProperties) throws RuntimeException {

		File savedFile = null;
		DataStore perimeterFiledataStore = null;
		DataStore predictionFileDataStore = null;
		DataStore layerExtentDataStore = null;
		
		long expectedSimulatedTime = scenarioProperties.getTimeToBeSimulated();

		// Step 1: Create map
		MapContent map = new MapContent();
		map.setTitle("Farsite Scenario");
		
		try {
			
			perimeterFiledataStore = ShapeFileReader.getDataStore(perimeterFile);
			predictionFileDataStore = ShapeFileReader.getDataStore(predictionFile);
			layerExtentDataStore = ShapeFileReader.getDataStore(landscapeExtentFile);
	
			Style style = SLD.createPointStyle("square", Color.red, Color.red, 1.0f, 8.0f);
			StyleBuilder styleBuilder = new StyleBuilder();
			String attributeName = "MINX";
			//Font font = styleBuilder.createFont("Times New Roman", 10.0);
			//TextSymbolizer textSymb = styleBuilder.createTextSymbolizer(Color.black, font, attributeName);
			//Rule rule = styleBuilder.createRule(textSymb);
			//style.featureTypeStyles().get(0).rules().add(rule);
	

			String[] typeNames1 = perimeterFiledataStore.getTypeNames();
			String typeName1 = typeNames1[0];
			FeatureSource featureSource1 = perimeterFiledataStore.getFeatureSource(typeName1);
			Layer perimeterLayer = new FeatureLayer(featureSource1, SLD.createLineStyle(Color.BLUE, 2));
			perimeterLayer.setTitle(perimeterFile.getName());

			String[] typeNames2 = predictionFileDataStore.getTypeNames();
			String typeName2 = typeNames2[0];
			FeatureSource featureSource2 = predictionFileDataStore.getFeatureSource(typeName2);
			Layer predictionLayer = new FeatureLayer(featureSource2, SLD.createLineStyle(Color.RED, 2));
			predictionLayer.setTitle(predictionFile.getName());

			String[] typeNames3 = layerExtentDataStore.getTypeNames();
			String typeName3 = typeNames3[0];
			FeatureSource featureSource3 = layerExtentDataStore.getFeatureSource(typeName3);
			Layer landscapeExtentLayer = new FeatureLayer(featureSource3, SLD.createLineStyle(Color.BLACK, 1));
			landscapeExtentLayer.setTitle(landscapeExtentFile.getName());

			map.addLayer(predictionLayer);
			map.addLayer(perimeterLayer);
			map.addLayer(landscapeExtentLayer);

			// Step 2: Set projection
			CoordinateReferenceSystem crs = CRS.decode(ScenarioProperties.CRS);
			MapViewport viewport = map.getViewport();
			viewport.setCoordinateReferenceSystem(crs);

			NormalizedSymmetricDifference nsdComparator = new NormalizedSymmetricDifference();
			nsdComparator.setIgnitionPerimeterFile(scenarioProperties.getPerimeterAtT0File());
			FarsiteIndividualEvaluator nsdEvaluator = new FarsiteIndividualEvaluator(nsdComparator);
			Pair<Long, Double> fireEvolution = nsdEvaluator.getFireEvolution(predictionFile, perimeterFile);
			Long simulatedTime = fireEvolution.getLeft();
			Double nsd = fireEvolution.getRight();
			Double nsdWeightedPredictionError = nsdEvaluator.calculateWeightedPredictionError(predictionFile, perimeterFile, expectedSimulatedTime);
			
			GoodnessOfFit gofComparator = new GoodnessOfFit();
			gofComparator.setIgnitionPerimeterFile(scenarioProperties.getPerimeterAtT0File());
			Double gof = gofComparator.compare(predictionFile, perimeterFile);
			
			GoodnessOfFit adjGofComparator = new AdjustedGoodnessOfFit();
			adjGofComparator.setIgnitionPerimeterFile(scenarioProperties.getPerimeterAtT0File());
			Double adjGof = adjGofComparator.compare(predictionFile, perimeterFile);
			
			String[] textToImage = { 
				"Pred File    : " + predictionFile.getName(), 
				"Sim Time     : " + simulatedTime,
				"Exp Sim Time : " + expectedSimulatedTime,
				"NSD          : " + nsd,
				"NSD Fitness  : " + nsdWeightedPredictionError,
				"GoF          : " + gof,
				"aGoF         : " + adjGof
			};

			// Step 3: Save image
			String jpgFileName = predictionFile.getAbsolutePath().replace(".shp", ".jpg");
			File jpgFile = new File(jpgFileName);
			
			boolean savedAsJPG = saveAsJPG(map, jpgFile, 800, textToImage);
			
			savedFile = savedAsJPG ? jpgFile : null;
			
		} catch (IOException | FactoryException | TopologyException e){
			logger.error("There was an exception when trying to generate the .jpg image for prediction file " + predictionFile.getAbsolutePath(), e);
			throw new RuntimeException(e);
		} finally {
			if (perimeterFiledataStore != null) {
				perimeterFiledataStore.dispose();
			}
			if (predictionFileDataStore != null) {
				predictionFileDataStore.dispose();
			}
			if (layerExtentDataStore != null) {
				layerExtentDataStore.dispose();
			}
			if (map != null) {
				map.dispose();
			}
		}
		
		return savedFile;
	}

	public static boolean saveAsJPG(final MapContent map, File outputFile, final int height, String[] textToImage) throws MalformedURLException, IOException {

		GTRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(map);

		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		try {
			mapBounds = map.getMaxBounds();
			//System.out.println(mapBounds.getSpan(1));
			//System.out.println(mapBounds.getSpan(0));
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			// imageBounds = new Rectangle( 0, 0, imageWidth, (int) Math.round(imageWidth *
			// heightToWidth) );
			int width = Math.max(1200,(int) Math.round(height * heightToWidth));
			imageBounds = new Rectangle(0, 0, width, height);

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

			addTextToImage(image, textToImage);

			//ImageIO.write(theImage, "png", new File("test.png"));
			return ImageIO.write(image, "jpeg", outputFile);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private static void addTextToImage(BufferedImage theImage, String[] theText)
			throws IOException, MalformedURLException {
		// final = ImageIO.read(new
		// URL("https://www.researchgate.net/profile/Ronny_Vallejos/publication/273362498/figure/fig1/AS:353314869923841@1461248237727/a-Original-image-Lenna-with-a-size-of-512-512-b-Transformation-of-the-original.png"));

		Graphics g = theImage.getGraphics();
		
		Font f = new Font(Font.MONOSPACED, Font.PLAIN, 18);
		//Font f = new Font("Courier New", Font.BOLD, 18);
		//g.setFont(g.getFont().deriveFont(20f));
		//Font("Courier", Font.Style.NORMAL)
		//Font font = g.getFont().;
		g.setFont(f);
		g.setColor(Color.BLACK);
		
		int leftMargin = 50;
		int verticalDistance = 100;
		
		for (int i=0; i < theText.length; i++) {
			g.drawString(theText[i], leftMargin, (i+1) * verticalDistance);
		}
		
		g.dispose();

	}

}
