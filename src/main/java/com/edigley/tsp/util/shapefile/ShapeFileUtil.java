package com.edigley.tsp.util.shapefile;

import java.io.File;

import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShapeFileUtil {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileUtil.class);
	
	public static void describe(File file) throws Exception {
		logger.info("describe.fileName: " + file.getAbsolutePath());

		Feature feature = ShapeFileReader.getLastFeature(file);
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

}
