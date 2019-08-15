package com.edigley.tsp.input;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
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

public class ShapeFileUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ShapeFileUtil.class);
	
	public static Geometry getGeometriesPoligon(File file) throws Exception {
		logger.info("getGeometriesPoligon.fileName: " + file.getAbsolutePath());
		Map<String, String> connect = new HashMap<>();
		connect.put("url", file.toURI().toString());

		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];

		logger.info("getGeometriesPoligon: Reading feature content: " + typeName);

		FeatureSource featureSource = dataStore.getFeatureSource(typeName);
		FeatureCollection features = featureSource.getFeatures();
		
		FeatureIterator it = features.features();

		int nOfFeatures = 0;
		
		GeometryFactory gf = new GeometryFactory();
		//Polygon pB = gf.createPolygon(lB.getCoordinates());
		//Geometry geometry = ((GeometryCollection) it.next().getDefaultGeometryProperty().getValue()).convexHull();
		Geometry geometry = gf.createPolygon(((GeometryCollection) it.next().getDefaultGeometryProperty().getValue()).getCoordinates());
		
		while (it.hasNext()) {
			nOfFeatures++;
			Feature feature = it.next();
			//geometry = geometry.union(((GeometryCollection)feature.getDefaultGeometryProperty().getValue())).convexHull();
			GeometryCollection other = (GeometryCollection)feature.getDefaultGeometryProperty().getValue();
			geometry = geometry.union(gf.createPolygon(other.getCoordinates()));
		}
		it.close();
		
		logger.info("getGeometriesPoligon.nOfFeatures: " + nOfFeatures);

		return geometry;
	}
	
	public static Object getGeometry(File file) throws Exception {

		Feature feature = getFirstFeature(file);
		GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
		return sourceGeometry.getValue();

	}

	public static Feature getFirstFeature(File file) throws Exception {
		logger.info("getFirstFeature.fileName: " + file.getAbsolutePath());
		Map<String, String> connect = new HashMap<>();
		connect.put("url", file.toURI().toString());

		DataStore dataStore = DataStoreFinder.getDataStore(connect);
		String[] typeNames = dataStore.getTypeNames();
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
		}
		it.close();
		
		logger.info("getFirstFeature.nOfFeatures: " + nOfFeatures);

		return feature;
	}
	
	public static void describe(File file) throws Exception {
		logger.info("describe.fileName: " + file.getAbsolutePath());

		Feature feature = getFirstFeature(file);
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
	
	public static void save(File file, MultiPolygon p2, CoordinateReferenceSystem crs)
			throws Exception, MalformedURLException, IOException {

		// Create simple feature type
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		ReferencingObjectFactory refFactory = new ReferencingObjectFactory();
		typeBuilder.setName("farsiteOutputAsPolygon");
		typeBuilder.setCRS(crs);
		typeBuilder.add("the_geom", MultiPolygon.class);
		SimpleFeatureType featureType = typeBuilder.buildFeatureType();

		// build feature collection
		List<SimpleFeature> features = new ArrayList<>();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
		featureBuilder.add(p2);// p1Feature.getDefaultGeometryProperty().getValue());
		features.add(featureBuilder.buildFeature("the-feature-id"));
		SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);

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

}
