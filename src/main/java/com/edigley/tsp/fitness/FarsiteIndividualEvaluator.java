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
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.util.ParserUtil;
import com.edigley.tsp.util.shapefile.ShapeFileReader;

public class FarsiteIndividualEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteIndividualEvaluator.class);
	
	private static FarsiteIndividualEvaluator instance = null;
	
	private ComparisonMethod comparator = null;
	
	private FarsiteIndividualEvaluator(ComparisonMethod comparator) {
		this.comparator = comparator;
	}
	
	public static FarsiteIndividualEvaluator getInstance() {
		if (instance == null) {
			instance = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference());
		}
		return instance;
	}
	
	public Pair<Long, Double> getFireEvolution(File fileA, File fileB) throws IOException {
		return ImmutablePair.of(getSimulatedTime(fileB), this.comparator.compare(fileA, fileB));
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

	public void setComparator(ComparisonMethod comparator) {
		this.comparator = comparator;
	}
	
}
