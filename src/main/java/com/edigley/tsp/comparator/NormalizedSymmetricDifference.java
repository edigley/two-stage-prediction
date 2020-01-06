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
	
	public Double compare(String gAFilePath, String gBFilePath) throws IOException {
		return compare(new File(gAFilePath), new File(gBFilePath));
	}
	
	public Double compare(File gAFile, File gBFile) throws IOException {
		MultiPolygon polygonA = (MultiPolygon) ShapeFileReader.getGeometry(gAFile);

		MultiPolygon multiPolygonB = ShapeFileUtil.toMultiPolygon(gBFile);
		
		Geometry symmetricDifference = polygonA.symDifference(multiPolygonB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / polygonA.getArea();
		
		return normalizedSymmetricDifference;
	}

}
