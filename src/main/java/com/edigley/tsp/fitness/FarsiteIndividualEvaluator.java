package com.edigley.tsp.fitness;

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
import com.edigley.tsp.util.ParserUtil;
import com.edigley.tsp.util.shapefile.ShapeFileReader;

public class FarsiteIndividualEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteIndividualEvaluator.class);
	
	private ComparisonMethod comparator = null;
	
	public FarsiteIndividualEvaluator(ComparisonMethod comparator) {
		this.comparator = comparator;
	}
	
	public Pair<Long, Double> getFireEvolution(File predictionFile, File perimeterFile) throws IOException {
		return getFireEvolution(predictionFile, perimeterFile, this.comparator);
	}

	public static Pair<Long, Double> getFireEvolution(File predictionFile, File perimeterFile, ComparisonMethod comparator) throws IOException {
		return ImmutablePair.of(extractSimulatedTime(predictionFile), comparator.compare(predictionFile, perimeterFile));
	}
	
	public Long getSimulatedTime(File file) throws IOException {
		return extractSimulatedTime(file);
	}
	
	public static Long extractSimulatedTime(File file) throws IOException {
		Feature lastFeature = ShapeFileReader.getLastFeature(file);
		// properties: the_geom, Fire_Type, Month, Day, Hour, Elapsed_Mi
		Double maxSimulatedTime = lastFeature.getProperties("Elapsed_Mi")
				.stream()
				.map(p -> Double.valueOf(p.getValue().toString()))
				.max(Comparator.comparing(Double::doubleValue))
				.orElseThrow(NoSuchElementException::new);
		return maxSimulatedTime.longValue();
	}

	public Double calculateWeightedPredictionError(File predictionFile, File perimeterFile, Long expectedSimulatedTime) {
		
		Double fireError = Double.NaN;
		
		try {
			
			Pair<Long, Double> fireEvolution = this.getFireEvolution(predictionFile, perimeterFile);
			
			Long effectivelySimulatedTime = fireEvolution.getKey();
			Double comparisonMetric = fireEvolution.getValue(); // nsd or gof
			
			Double factor = comparator.defineAdjustmentFactor(effectivelySimulatedTime, expectedSimulatedTime);
			
			fireError = ParserUtil.parseDouble(factor * comparisonMetric);
				
		} catch (Exception e) {
			System.err.printf("Couldn't compare non-finished scenario result for individual. Error message: %s\n",
					e.getMessage());
			logger.error("Couldn't compare non-finished scenario result", e);
		}

		return fireError;
	}

	public void setComparator(ComparisonMethod comparator) {
		this.comparator = comparator;
	}

	public ComparisonMethod getComparator() {
		return comparator;
	}
	
}
