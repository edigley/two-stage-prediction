package com.edigley.tsp.input;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Font;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

public class ShapeFileUtil {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileUtil.class);

	public static Geometry getGeometriesPoligon(File file) throws IOException {
		logger.info("getGeometriesPoligon.fileName: " + file.getAbsolutePath());
		Map<String, String> connect = new HashMap<>();
		connect.put("url", file.toURI().toString());

		DataStore dataStore = DataStoreFinder.getDataStore(connect);

		try {
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

			logger.info("getGeometriesPoligon: Reading feature content: " + typeName);

			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection features = featureSource.getFeatures();

			FeatureIterator it = features.features();

			int nOfFeatures = 0;

			GeometryFactory gf = new GeometryFactory();
			// Polygon pB = gf.createPolygon(lB.getCoordinates());
			// Geometry geometry = ((GeometryCollection)
			// it.next().getDefaultGeometryProperty().getValue()).convexHull();
			Geometry geometry = gf.createPolygon(
					((GeometryCollection) it.next().getDefaultGeometryProperty().getValue()).getCoordinates());

			try {
				while (it.hasNext()) {
					nOfFeatures++;
					Feature feature = it.next();
					// geometry =
					// geometry.union(((GeometryCollection)feature.getDefaultGeometryProperty().getValue())).convexHull();
					GeometryCollection other = (GeometryCollection) feature.getDefaultGeometryProperty().getValue();
					try {
						geometry = geometry.union(gf.createPolygon(other.getCoordinates()));
					} catch (TopologyException | IllegalArgumentException e) {
						logger.warn(e.getClass().getSimpleName() + ": Could'not perform union with geometry for file: "
								+ file.getAbsolutePath(), e);
					} catch (Exception e) {
						logger.warn(
								"Exception: Could'not perform union with geometry for file: " + file.getAbsolutePath(),
								e);
					}
				}
			} finally {
				it.close();
			}

			logger.info("getGeometriesPoligon.nOfFeatures: " + nOfFeatures);
			return geometry;

		} finally {
			dataStore.dispose();
		}
	}

	public static Object getGeometry(File file) throws IOException {
		Feature feature = getLastFeature(file);
		GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
		return sourceGeometry.getValue();
	}

	public static Feature getFeature(File file) throws Exception {
		Feature feature = getLastFeature(file);
		return feature;
	}

	public static Feature getLastFeature(File file) throws IOException {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(allFeatures.size() - 1);
	}

	public static Feature getFirstFeature(File file) throws Exception {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(0);
	}

	public static Pair<Long, Double> getFireEvolution(File fileA, File fileB) throws IOException {
		return ImmutablePair.of(getSimulatedTime(fileB), calculateNormalizedSymmetricDifference(fileA, fileB));
	}

	public static Double calculateWeightedPredictionError(File gAFile, File gBFile, Long simulationTime) {
		// System.err.printf("fireError.equals(Double.NaN) or fireError > 9999: " +
		// fireError + "\n");
		Double fireError = Double.NaN;
		logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
		// File gAFile = scenarioProperties.getPerimeterAtT1();
		// File gBFile = scenarioProperties.getShapeFileOutput(generation, id);
		try {
			Pair<Long, Double> fireEvolution = ShapeFileUtil.getFireEvolution(gAFile, gBFile);
			Long effectivelySimulatedTime = fireEvolution.getKey();
			Double _fireError = fireEvolution.getValue();
			Double factor = Math.max(1.0, simulationTime / (effectivelySimulatedTime * 1.0));
			// Double _fireError = ShapeFileUtil.calculatePredictionError(gAFile, gBFile);
			if (fireError.equals(Double.MAX_VALUE)) {
				// fireError = (1 + _fireError);
				fireError = Double.parseDouble(String.format("%.6f", (factor * _fireError)).replace(",", "."));
			} else {
				fireError = Double.parseDouble(String.format("%.6f", (factor * _fireError)).replace(",", "."));
			}
		} catch (Exception e) {
			System.err.printf("Couldn't compare non-finished scenario result for individual. Error message: %s\n",
					e.getMessage());
			logger.error("Couldn't compare non-finished scenario result", e);
		}

		return fireError;
	}

	public static Long getSimulatedTime(File file) throws IOException {
		Feature lastFeature = getLastFeature(file);
		// properties: the_geom, Fire_Type, Month, Day, Hour, Elapsed_Mi
		Double maxSimulatedTime = lastFeature.getProperties("Elapsed_Mi").stream()
				.map(p -> Double.valueOf(p.getValue().toString())).max(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);
		return maxSimulatedTime.longValue();
	}

	public static List<Feature> getAllFeatures(File file) throws IOException {
		logger.info("getFirstFeature.fileName: " + file.getAbsolutePath());
		Map<String, String> connect = new HashMap<>();
		connect.put("url", file.toURI().toString());

		List<Feature> allFeatures = new ArrayList<>();
		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		try {
			String[] typeNames = dataStore.getTypeNames();
			// Arrays.stream(typeNames).forEach(v -> System.out.println("typeNames: " + v));
			String typeName = typeNames[0];

			logger.info("getFirstFeature: Reading feature content: " + typeName);

			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection features = featureSource.getFeatures();

			FeatureIterator it = features.features();

			Feature feature = null;

			int nOfFeatures = 0;
			while (it.hasNext()) {
				nOfFeatures++;
				feature = it.next();
				allFeatures.add(feature);
			}
			it.close();
			logger.info("getFirstFeature.nOfFeatures: " + nOfFeatures);
			return allFeatures;
		} catch (IOException e) {
			logger.error("Error when getting all features from file " + file.getAbsolutePath() + ": " + e.getMessage(),
					e);
			throw e;
		} finally {
			dataStore.dispose();
		}
	}

	public static FeatureSource getFeatureSource(File file) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("url", file.toURI().toString());

		DataStore dataStore = DataStoreFinder.getDataStore(params);

		FeatureSource featureSource = null;
		try {
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];
			featureSource = dataStore.getFeatureSource(typeName);
		} catch (IOException e) {
			logger.error("Error when getting all features from file " + file.getAbsolutePath() + ": " + e.getMessage(),
					e);
			throw e;
		} finally {
			dataStore.dispose();
		}

		return featureSource;
	}

	public static void describe(File file) throws Exception {
		logger.info("describe.fileName: " + file.getAbsolutePath());

		Feature feature = getLastFeature(file);
		logger.info("---> getPolygon.feature.getType(): " + feature.getType());
		logger.info("---> feature.getProperties().size(): " + feature.getProperties().size());
		feature.getProperties().stream().forEach(p -> logger.info(p.getName().toString()));
		GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
		Object polygon = sourceGeometry.getValue();

		logger.info("---> sourceGeometry.name: " + sourceGeometry.getName());
		logger.info("---> sourceGeometry.descriptor: " + sourceGeometry.getDescriptor());
		logger.info("---> sourceGeometry.identifier: " + sourceGeometry.getIdentifier());
		logger.info("---> sourceGeometry.type: " + sourceGeometry.getType());
		logger.info("---> sourceGeometry.userData: " + sourceGeometry.getUserData());
		logger.info("---> sourceGeometry.getDescriptor().getCoordinateReferenceSystem(): "
				+ sourceGeometry.getDescriptor().getCoordinateReferenceSystem());
		logger.info("---> sourceGeometry.bounds: " + sourceGeometry.getBounds());
		// Point centroid = ((GeometryAttributeImpl) sourceGeometry).getCentroid();
		// logger.info("---> centroid: " + centroid.getDimension());
	}

	public static void save(File file, Polygon polygon, CoordinateReferenceSystem crs)
			throws Exception, MalformedURLException, IOException {

		GeometryFactory gf = new GeometryFactory();

		Polygon[] polys = new Polygon[1];
		polys[0] = polygon;

		MultiPolygon multiPolygon = gf.createMultiPolygon(polys);

		save(file, multiPolygon, crs);
	}

	public static void save(File file, MultiPolygon multiPolygon, CoordinateReferenceSystem crs)
			throws MalformedURLException, IOException {

		SimpleFeatureType featureType = createSimpleFeatureType(crs);

		SimpleFeatureCollection collection = buildFeatureCollection(multiPolygon, featureType);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		Map<String, Serializable> params = new HashMap<>();
		params.put("url", file.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		newDataStore.createSchema(featureType);
		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
		// write features to file
		try {
			if (!SimpleFeatureStore.class.isInstance(featureSource)) {
				throw new IOException(typeName + " does not support read/write access");
			} else {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				Transaction transaction = new DefaultTransaction("create");
				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(collection);
					transaction.commit();
					transaction.close();
				} catch (IOException e) {
					logger.error("Error when trying to commit/clos the transaction. Going to perform a rollback...", e);
					transaction.rollback();
					transaction.close();
					logger.error("A rollback was successfully performed");
					throw e;
				}
			}
		} catch (IOException e) {
			logger.error("Error when closing the transaction", e);
			throw e;
		} finally {
			newDataStore.dispose();
		}
	}

	private static SimpleFeatureCollection buildFeatureCollection(MultiPolygon mp, SimpleFeatureType featureType) {
		// build feature collection
		List<SimpleFeature> features = new ArrayList<>();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
		featureBuilder.add(mp);// p1Feature.getDefaultGeometryProperty().getValue());
		features.add(featureBuilder.buildFeature("the-feature-id"));
		SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);
		return collection;
	}

	private static SimpleFeatureType createSimpleFeatureType(CoordinateReferenceSystem crs) {
		// Create simple feature type
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		ReferencingObjectFactory refFactory = new ReferencingObjectFactory();
		typeBuilder.setName("farsiteOutputAsPolygon");
		typeBuilder.setCRS(crs);
		typeBuilder.add("the_geom", MultiPolygon.class);
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();
		return featureType;
	}

	public static Double calculateNormalizedSymmetricDifference(String gAFilePath, String gBFilePath)
			throws IOException {
		return calculateNormalizedSymmetricDifference(new File(gAFilePath), new File(gBFilePath));
	}

	public static Double calculateNormalizedSymmetricDifference(File gAFile, File gBFile) throws IOException {

		MultiPolygon pA = (MultiPolygon) ShapeFileUtil.getGeometry(gAFile);
		logger.info("---> pA.getArea(): " + pA.getArea());

		// MultiLineString l2 = (MultiLineString) getGeometry(p2FileName);
		// MultiLineString lB = (MultiLineString) getGeometriesPoligon(p2FileName);
		// System.out.println("---> lB.getArea(): " + lB.getArea());
		// GeometryFactory gf = new GeometryFactory();
		// Polygon pB = gf.createPolygon(lB.getCoordinates());

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gBFile);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
		} catch (ClassCastException e) {
			try {
				pB = (Polygon) ShapeFileUtil.getGeometriesPoligon(gBFile);
				logger.info("---> pB.getArea(): " + pB.getArea());
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + gBFile.getAbsolutePath(), e2);
			}
		}

		// GeometryFunction functions = new GeometryFunction();
		// GeometryFunctions
		// MultiPolygon p = (MultiPolygon)
		// getPolygon("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp");
		Geometry symDiff = pA.symDifference((mpB != null) ? mpB : pB);
		double symDiffArea = symDiff.getArea();
		logger.info("---> (pB symDiff pA).getArea(): " + symDiffArea);
		double predictionError = symDiffArea / pA.getArea();
		logger.info("---> predictionError: " + predictionError);
		return predictionError;

	}

	public static void saveGeometryPolygon(File gFile, File outputFile) throws Exception {
		Feature p1Feature = ShapeFileUtil.getLastFeature(gFile);
		CoordinateReferenceSystem crs = p1Feature.getDefaultGeometryProperty().getDescriptor()
				.getCoordinateReferenceSystem();

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gFile);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
			ShapeFileUtil.save(outputFile, mpB, null);
		} catch (ClassCastException e) {
			pB = (Polygon) ShapeFileUtil.getGeometriesPoligon(gFile);
			ShapeFileUtil.save(outputFile, pB, null);
			logger.info("---> pB.getArea(): " + pB.getArea());
		}

		// MultiPolygon pB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gFile);
		// ShapeFileUtil.save(outputFile, ShapeFileUtil.getGeometriesPoligon(gFile),
		// crs);
	}

	public static boolean boundariesTouch(File firePerimeter, File layerExtentFile) throws IOException {
		Polygon layerExtent = (Polygon) ShapeFileUtil.getGeometriesPoligon(layerExtentFile);
		// Polygon shapeInternal = (Polygon)
		// ShapeFileUtil.getGeometriesPoligon(shapeInternalPolygonFile);

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(firePerimeter);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
		} catch (ClassCastException e) {
			try {
				pB = (Polygon) ShapeFileUtil.getGeometriesPoligon(firePerimeter);
				logger.info("---> pB.getArea(): " + pB.getArea());
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + firePerimeter.getAbsolutePath(), e2);
				return false;
			}
		}

		if (mpB != null) {
			return layerExtent.getExteriorRing().isWithinDistance(mpB, 1);
		} else {
			return layerExtent.getExteriorRing().isWithinDistance(pB, 1);
		}

	}

	public static DataStore getDataStore(File shapeFile) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("url", shapeFile.toURI().toString());
		return DataStoreFinder.getDataStore(params);
	}

	public static void saveFarsiteResultAsJPG(File p1File, File shapeFile, File layerExtentFile)
			throws IOException, NoSuchAuthorityCodeException, FactoryException {
		// Step 1: Create map
		MapContent map = new MapContent();
		map.setTitle("Farsite Scenario");

		DataStore dataStore1 = ShapeFileUtil.getDataStore(p1File);
		DataStore dataStore2 = ShapeFileUtil.getDataStore(shapeFile);
		DataStore dataStore3 = ShapeFileUtil.getDataStore(layerExtentFile);

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
			layer1.setTitle(p1File.getName());

			String[] typeNames2 = dataStore2.getTypeNames();
			String typeName2 = typeNames2[0];
			FeatureSource featureSource2 = dataStore2.getFeatureSource(typeName2);
			Layer layer2 = new FeatureLayer(featureSource2, SLD.createLineStyle(Color.RED, 2));
			layer2.setTitle(shapeFile.getName());

			String[] typeNames3 = dataStore3.getTypeNames();
			String typeName3 = typeNames3[0];
			FeatureSource featureSource3 = dataStore3.getFeatureSource(typeName3);
			Layer layer3 = new FeatureLayer(featureSource3, SLD.createLineStyle(Color.BLACK, 1));
			layer3.setTitle(layerExtentFile.getName());

			map.addLayer(layer2);
			map.addLayer(layer1);
			map.addLayer(layer3);

			// Step 2: Set projection
			CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
			MapViewport vp = map.getViewport();
			vp.setCoordinateReferenceSystem(crs);

			Pair<Long, Double> fireEvolution = getFireEvolution(p1File, shapeFile);
			Long simulatedTime = fireEvolution.getLeft();
			Double normalizedSymmetricDifference = fireEvolution.getRight();
			Double weightedPredictionError = calculateWeightedPredictionError(p1File, shapeFile, simulatedTime);
			String[] textToImage = { "Simulated Time: " + simulatedTime,
					"Normalized Symmetric Difference: " + normalizedSymmetricDifference,
					"Fitness: " + weightedPredictionError };

			// Step 3: Save image
			ShapeFileUtil.saveAsJPG(map, shapeFile.getName().replace(".shp", ".jpg"), 800, textToImage);

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
