package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class NormalizedSymmetricDifference implements ComparisonMethod {

	private static final Logger logger = LoggerFactory.getLogger(NormalizedSymmetricDifference.class);
	
	public Double compare(String predictionFilePath, String perimeterFilePath) throws IOException {
		return compare(new File(predictionFilePath), new File(perimeterFilePath));
	}
	
	public Double compare(File predictionFile, File perimeterFile) throws IOException {
		MultiPolygon mpA = ShapeFileUtil.toMultiPolygon(perimeterFile);

		MultiPolygon mpB = ShapeFileUtil.toMultiPolygon(predictionFile);
		
		Geometry symmetricDifference = mpA.symDifference(mpB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / mpA.getArea();
		
		return normalizedSymmetricDifference;
	}

}
