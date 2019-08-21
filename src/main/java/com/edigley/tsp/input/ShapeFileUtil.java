package com.edigley.tsp.input;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

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
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.geotools.util.Comparators;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.calibration.GeneticAlgorithm;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

public class ShapeFileUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ShapeFileUtil.class);
	
	public static Geometry getGeometriesPoligon(File file) throws Exception {
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

			while (it.hasNext()) {
				nOfFeatures++;
				Feature feature = it.next();
				// geometry =
				// geometry.union(((GeometryCollection)feature.getDefaultGeometryProperty().getValue())).convexHull();
				GeometryCollection other = (GeometryCollection) feature.getDefaultGeometryProperty().getValue();
				try {
					geometry = geometry.union(gf.createPolygon(other.getCoordinates()));
				} catch (TopologyException e) {
					logger.warn("Could'not perform union with geometry", e);
				}
			}
			it.close();

			logger.info("getGeometriesPoligon.nOfFeatures: " + nOfFeatures);

			return geometry;

		} finally {
			dataStore.dispose();
		}
	}
	
	public static Object getGeometry(File file) throws Exception {
		Feature feature = getLastFeature(file);
		GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
		return sourceGeometry.getValue();
	}

	public static Feature getFeature(File file) throws Exception {
		Feature feature = getLastFeature(file);
		return feature;
	}
	
	public static Feature getLastFeature(File file) throws Exception {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(allFeatures.size()-1);
	}
	
	public static Feature getFirstFeature(File file) throws Exception {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(0);
	}
	
	public static Double getSimulatedTime(File file) throws Exception {
		Feature lastFeature = getLastFeature(file);
		//properties: the_geom, Fire_Type, Month, Day, Hour, Elapsed_Mi
		Double maxSimulatedTime = lastFeature.getProperties("Elapsed_Mi")
				.stream()
				.map(p -> Double.valueOf(p.getValue().toString()))
				.max(Comparator.comparing(Double::doubleValue)).orElseThrow(NoSuchElementException::new);
		return maxSimulatedTime;
	}	
	
	public static List<Feature> getAllFeatures(File file) throws Exception {
		logger.info("getFirstFeature.fileName: " + file.getAbsolutePath());
		Map<String, String> connect = new HashMap<>();
		connect.put("url", file.toURI().toString());

		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
		//Arrays.stream(typeNames).forEach(v -> System.out.println("typeNames: " + v));
		String typeName = typeNames[0];

		logger.info("getFirstFeature: Reading feature content: " + typeName);

		FeatureSource featureSource = dataStore.getFeatureSource(typeName);
		FeatureCollection features = featureSource.getFeatures();
		
		List<Feature> allFeatures = new ArrayList<>();
		
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
		logger.info("---> sourceGeometry.getDescriptor().getCoordinateReferenceSystem(): " + sourceGeometry.getDescriptor().getCoordinateReferenceSystem());
		logger.info("---> sourceGeometry.bounds: " + sourceGeometry.getBounds());
		// Point centroid = ((GeometryAttributeImpl) sourceGeometry).getCentroid();
		// logger.info("---> centroid: " + centroid.getDimension());
	}
	
	public static void save(File file, Polygon polygon, CoordinateReferenceSystem crs) throws Exception, MalformedURLException, IOException {
		
		GeometryFactory gf = new GeometryFactory();
		
		Polygon[] polys = new Polygon[1];
		polys[0] = polygon;
		
		MultiPolygon multiPolygon = gf.createMultiPolygon(polys);
		
		save(file, multiPolygon, crs);
	}
	
	public static void save(File file, MultiPolygon multiPolygon, CoordinateReferenceSystem crs)
			throws Exception, MalformedURLException, IOException {

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
				throw new Exception(typeName + " does not support read/write access");
			} else {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				Transaction transaction = new DefaultTransaction("create");
				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(collection);
					transaction.commit();
					transaction.close();
				} catch (Exception e) {
					transaction.rollback();
					transaction.close();
					throw e;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			newDataStore.dispose();
		}
	}

	private static SimpleFeatureCollection buildFeatureCollection(MultiPolygon multiPolygon,
			SimpleFeatureType featureType) {
		// build feature collection
		List<SimpleFeature> features = new ArrayList<>();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
		featureBuilder.add(multiPolygon);// p1Feature.getDefaultGeometryProperty().getValue());
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
	
	public static Double calculatePredictionError(File gAFile, File gBFile) throws Exception {
			
			MultiPolygon pA = (MultiPolygon) ShapeFileUtil.getGeometry(gAFile);
			logger.info("---> pA.getArea(): " + pA.getArea());

			//MultiLineString l2 = (MultiLineString) getGeometry(p2FileName);
			//MultiLineString lB = (MultiLineString) getGeometriesPoligon(p2FileName);
			//System.out.println("---> lB.getArea(): " + lB.getArea());
			//GeometryFactory gf = new GeometryFactory();
			//Polygon pB = gf.createPolygon(lB.getCoordinates());
			
			MultiPolygon mpB = null;
			Polygon pB = null;
			
			try {
				mpB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gBFile);
				logger.info("---> mpB.getArea(): " + mpB.getArea());
			} catch (ClassCastException e) {
				pB = (Polygon) ShapeFileUtil.getGeometriesPoligon(gBFile);
				logger.info("---> pB.getArea(): " + pB.getArea());
			}

			// GeometryFunction functions = new GeometryFunction();
			// GeometryFunctions
			// MultiPolygon p = (MultiPolygon)
			// getPolygon("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp");
			Geometry symDiff = pA.symDifference( (mpB != null) ? mpB : pB );
			double symDiffArea = symDiff.getArea();
			logger.info("---> (pB symDiff pA).getArea(): " + symDiffArea);
			double predictionError = symDiffArea / pA.getArea();
			logger.info("---> predictionError: " + predictionError);
			return predictionError;

	}
	
	public static void saveGeometryPolygon(File gFile, File outputFile) throws Exception {
		Feature p1Feature = ShapeFileUtil.getLastFeature(gFile);
		CoordinateReferenceSystem crs = p1Feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
		
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
		
		//MultiPolygon pB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gFile);
		//ShapeFileUtil.save(outputFile, ShapeFileUtil.getGeometriesPoligon(gFile), crs);
	}

}
