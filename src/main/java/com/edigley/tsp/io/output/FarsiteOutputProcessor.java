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

import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class FarsiteOutputProcessor {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteOutputProcessor.class);

	public static Double calculateNormalizedSymmetricDifference(String gAFilePath, String gBFilePath)
			throws IOException {
		return calculateNormalizedSymmetricDifference(new File(gAFilePath), new File(gBFilePath));
	}

	public static Double calculateNormalizedSymmetricDifference(File gAFile, File gBFile) throws IOException {

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
	
	public static Pair<Long, Double> getFireEvolution(File fileA, File fileB) throws IOException {
		return ImmutablePair.of(getSimulatedTime(fileB), calculateNormalizedSymmetricDifference(fileA, fileB));
	}

	public static Long getSimulatedTime(File file) throws IOException {
		Feature lastFeature = ShapeFileReader.getLastFeature(file);
		// properties: the_geom, Fire_Type, Month, Day, Hour, Elapsed_Mi
		Double maxSimulatedTime = lastFeature.getProperties("Elapsed_Mi").stream()
				.map(p -> Double.valueOf(p.getValue().toString())).max(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);
		return maxSimulatedTime.longValue();
	}

	public static Double calculateWeightedPredictionError(File gAFile, File gBFile, Long simulationTime) {
		// System.err.printf("fireError.equals(Double.NaN) or fireError > 9999: " +
		// fireError + "\n");
		Double fireError = Double.NaN;
		logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
		// File gAFile = scenarioProperties.getPerimeterAtT1();
		// File gBFile = scenarioProperties.getShapeFileOutput(generation, id);
		try {
			Pair<Long, Double> fireEvolution = getFireEvolution(gAFile, gBFile);
			Long effectivelySimulatedTime = fireEvolution.getKey();
			Double _fireError = fireEvolution.getValue();
			Double factor = Math.max(1.0, simulationTime / (effectivelySimulatedTime * 1.0));
			// Double _fireError = ShapeFileUtil.calculatePredictionError(gAFile, gBFile);
			if (fireError.equals(Double.MAX_VALUE)) {
				// fireError = (1 + _fireError);
				fireError = Double.parseDouble(String.format("%.6f", (factor * _fireError)).replace(",", "."));
			} else {
				fireError = Double.parseDouble(String.format("%.6f", (factor * _fireError)).replace(",", "."));
			}
		} catch (Exception e) {
			System.err.printf("Couldn't compare non-finished scenario result for individual. Error message: %s\n",
					e.getMessage());
			logger.error("Couldn't compare non-finished scenario result", e);
		}

		return fireError;
	}
	
}
