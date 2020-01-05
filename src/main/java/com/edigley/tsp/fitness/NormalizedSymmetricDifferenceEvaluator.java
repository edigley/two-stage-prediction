package com.edigley.tsp.fitness;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
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

		MultiPolygon multiPolygonB = ShapeFileUtil.toMultiPolygon(gBFile);
		
		Geometry symmetricDifference = polygonA.symDifference(multiPolygonB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / polygonA.getArea();
		
		return normalizedSymmetricDifference;
	}

}
