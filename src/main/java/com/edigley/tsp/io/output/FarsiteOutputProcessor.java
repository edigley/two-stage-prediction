package com.edigley.tsp.io.output;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.opengis.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.util.ParserUtil;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class FarsiteOutputProcessor {

	private static FarsiteOutputProcessor instance = null;
	
	private ComparisonMethod comparator = null;
	
	private FarsiteOutputProcessor(ComparisonMethod comparator) {
		this.comparator = comparator;
	}
	
	public static FarsiteOutputProcessor getInstance() {
		if (instance == null) {
			instance = new FarsiteOutputProcessor(new NormalizedSymmetricDifference());
		}
		return instance;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(FarsiteOutputProcessor.class);

	@Deprecated
	private static Double calculateNormalizedSymmetricDifference(String gAFilePath, String gBFilePath)
			throws IOException {
		return calculateNormalizedSymmetricDifference(new File(gAFilePath), new File(gBFilePath));
	}

	@Deprecated
	private static Double calculateNormalizedSymmetricDifference(File gAFile, File gBFile) throws IOException {

		MultiPolygon pA = (MultiPolygon) ShapeFileReader.getGeometry(gAFile);
		logger.info("---> pA.getArea(): " + pA.getArea());

		// MultiLineString l2 = (MultiLineString) getGeometry(p2FileName);
		// MultiLineString lB = (MultiLineString) getGeometriesPoligon(p2FileName);
		// System.out.println("---> lB.getArea(): " + lB.getArea());
		// GeometryFactory gf = new GeometryFactory();
		// Polygon pB = gf.createPolygon(lB.getCoordinates());

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(gBFile);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
		} catch (ClassCastException e) {
			try {
				pB = (Polygon) ShapeFileReader.getGeometriesPoligon(gBFile);
				logger.info("---> pB.getArea(): " + pB.getArea());
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + gBFile.getAbsolutePath(), e2);
			}
		}

		// GeometryFunction functions = new GeometryFunction();
		// GeometryFunctions
		// MultiPolygon p = (MultiPolygon)
		// getPolygon("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp");
		Geometry symDiff = pA.symDifference((mpB != null) ? mpB : pB);
		double symDiffArea = symDiff.getArea();
		logger.info("---> (pB symDiff pA).getArea(): " + symDiffArea);
		double predictionError = symDiffArea / pA.getArea();
		logger.info("---> predictionError: " + predictionError);
		return predictionError;

	}

	/*
	public Pair<Long, Double> getFireEvolution(File perimeterFile, File predictionFile) throws IOException {
		return ImmutablePair.of(getSimulatedTime(predictionFile), this.comparator.compare(perimeterFile, predictionFile));
	}

	public Long getSimulatedTime(File file) throws IOException {
		Feature lastFeature = ShapeFileReader.getLastFeature(file);
		// properties: the_geom, Fire_Type, Month, Day, Hour, Elapsed_Mi
		Double maxSimulatedTime = lastFeature.getProperties("Elapsed_Mi")
				.stream()
				.map(p -> Double.valueOf(p.getValue().toString()))
				.max(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);
		return maxSimulatedTime.longValue();
	}
	
	public Double calculateWeightedPredictionError(File gAFile, File gBFile, Long expectedSimulatedTime) {
		
		Double fireError = Double.NaN;
		
		try {
			
			Pair<Long, Double> fireEvolution = this.getFireEvolution(gAFile, gBFile);
			
			Long effectivelySimulatedTime = fireEvolution.getKey();
			Double normalizedSymmetricDifference = fireEvolution.getValue();
			
			Double factor = Math.max(1.0, expectedSimulatedTime / (effectivelySimulatedTime * 1.0));
			
			fireError = ParserUtil.parseDouble(factor * normalizedSymmetricDifference);
				
		} catch (Exception e) {
			System.err.printf("Couldn't compare non-finished scenario result for individual. Error message: %s\n",
					e.getMessage());
			logger.error("Couldn't compare non-finished scenario result", e);
		}

		return fireError;
	}

	*/
	
}
