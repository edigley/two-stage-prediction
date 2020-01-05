package com.edigley.tsp.util.shapefile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.factory.ReferencingObjectFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class ShapeFileWriter {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileWriter.class);

	public static void saveGeometryPolygon(File geometryFile, File outputFile) throws Exception {
		Feature p1Feature = ShapeFileReader.getLastFeature(geometryFile);
		CoordinateReferenceSystem crs = p1Feature.getDefaultGeometryProperty().getDescriptor()
				.getCoordinateReferenceSystem();

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(geometryFile);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
			save(outputFile, mpB, null);
		} catch (ClassCastException e) {
			pB = (Polygon) ShapeFileReader.getGeometriesPoligon(geometryFile);
			save(outputFile, pB, null);
			logger.info("---> pB.getArea(): " + pB.getArea());
		}

		// MultiPolygon pB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gFile);
		// ShapeFileUtil.save(outputFile, ShapeFileUtil.getGeometriesPoligon(gFile),
		// crs);
	}

	public static void save(File file, Polygon polygon, CoordinateReferenceSystem crs)
			throws Exception, MalformedURLException, IOException {

		MultiPolygon multiPolygon = ShapeFileUtil.wrapInAMultiPolygon(polygon);

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

	public static void saveAsPolygon(File file, Polygon polygonA, CoordinateReferenceSystem decode) {
		// TODO Auto-generated method stub
		
	}
	
}
