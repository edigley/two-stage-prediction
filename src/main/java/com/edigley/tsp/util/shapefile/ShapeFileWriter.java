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
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

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
			ShapeFileUtil.save(outputFile, mpB, null);
		} catch (ClassCastException e) {
			pB = (Polygon) ShapeFileReader.getGeometriesPoligon(geometryFile);
			ShapeFileUtil.save(outputFile, pB, null);
			logger.info("---> pB.getArea(): " + pB.getArea());
		}

		// MultiPolygon pB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(gFile);
		// ShapeFileUtil.save(outputFile, ShapeFileUtil.getGeometriesPoligon(gFile),
		// crs);
	}

}
