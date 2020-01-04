package com.edigley.tsp.fitness;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class NormalizedSymmetricDifferenceEvaluator implements IndividualEvaluator{

	private static final Logger logger = LoggerFactory.getLogger(NormalizedSymmetricDifferenceEvaluator.class);
	
	public Double evaluate(String gAFilePath, String gBFilePath) throws IOException {
		return evaluate(new File(gAFilePath), new File(gBFilePath));
	}
	
	public Double evaluate(File gAFile, File gBFile) throws IOException {
		MultiPolygon polygonA = (MultiPolygon) ShapeFileReader.getGeometry(gAFile);

		MultiPolygon multiPolygonB = null;
		Polygon polygonB = null;

		try {
			multiPolygonB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(gBFile);
		} catch (ClassCastException e) {
			try {
				polygonB = (Polygon) ShapeFileReader.getGeometriesPoligon(gBFile);
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + gBFile.getAbsolutePath(), e2);
			}
		}

		Geometry symmetricDifference = polygonA.symDifference((multiPolygonB != null) ? multiPolygonB : polygonB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / polygonA.getArea();
		
		return normalizedSymmetricDifference;
	}

}
