package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class NormalizedSymmetricDifference implements ComparisonMethod {

	private static final Logger logger = LoggerFactory.getLogger(NormalizedSymmetricDifference.class);
	
	private File ignitionPerimeterFile;
	
	private MultiPolygon ignitionPerimeterMap;
	
	public Double compare(String predictionFilePath, String perimeterFilePath) throws IOException {
		return compare(new File(predictionFilePath), new File(perimeterFilePath));
	}
	
	public Double compare(File predictionFile, File perimeterFile) throws IOException {
		MultiPolygon mpA = ShapeFileUtil.toMultiPolygon(perimeterFile);

		MultiPolygon mpB = ShapeFileUtil.toMultiPolygon(predictionFile);
		
		if (this.ignitionPerimeterMap != null) {
			mpA = ShapeFileUtil.toMultiPolygon(mpA.difference(this.ignitionPerimeterMap));
			mpB = ShapeFileUtil.toMultiPolygon(mpB.difference(this.ignitionPerimeterMap));
		}
		
		Geometry symmetricDifference = mpA.symDifference(mpB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / mpA.getArea();
		
		return normalizedSymmetricDifference;
	}

	@Override
	public Double defineAdjustmentFactor(Long effectivelySimulatedTime, Long expectedSimulatedTime) {
		Double factor = Math.max(1.0, expectedSimulatedTime / (effectivelySimulatedTime * 1.0));
		return factor;
	}
	
	@Override
	public int compare(FarsiteExecution e1, FarsiteExecution e2) {
		return e1.getFireError().compareTo(e2.getFireError());
	}

	@Override
	public void setIgnitionPerimeterFile(File ignitionPerimeterFile) {
		this.ignitionPerimeterFile = ignitionPerimeterFile;
		try {
			this.ignitionPerimeterMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(this.ignitionPerimeterFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
