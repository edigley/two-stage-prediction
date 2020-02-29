package com.edigley.tsp.util.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

public class ShapeFileReader {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileReader.class);

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
			Geometry geometry;
			Feature next = it.next();
			try {
				GeometryCollection value = (GeometryCollection) next.getDefaultGeometryProperty().getValue();
				Coordinate[] coordinates = value.getCoordinates();
				geometry = gf.createPolygon(coordinates);
			} catch (ClassCastException e) {
				Point thePoint = (Point) next.getDefaultGeometryProperty().getValue();
				geometry = gf.createPolygon(thePoint.buffer(0.000000001).getCoordinates());
			}

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

	public static Feature getFirstFeature(File file) throws Exception {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(0);
	}
	
	public static Feature getLastFeature(File file) throws IOException {
		List<Feature> allFeatures = getAllFeatures(file);
		return allFeatures.get(allFeatures.size() - 1);
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
	
	public static DataStore getDataStore(File shapeFile) throws IOException {
		Map<String, String> params = new HashMap<>();
		params.put("url", shapeFile.toURI().toString());
		return DataStoreFinder.getDataStore(params);
	}

}
