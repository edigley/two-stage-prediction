package com.edigley.tsp.comparator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;

public class NormalizedSymmetricDifferenceTest {

	private File resourcesDir = new File("src/test/resources/");

	private FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference());

	@Test
	public void testCalculatePredictionError() throws Exception {
		assertPredictionNSD("shape_1_3.shp", 180, 0.9148345582325907, 480, 2.439559);
	}

	private void assertPredictionNSD(String shapeFilePath, int simulationTime, double predictionError,
			int totalSimulationTime, double fitnessEvaluation) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, shapeFilePath);

		Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(shapeFile, perimeter1File);

		// System.out.printf("%s Error: %s \n", shapeFilePath, Error);
		Long time = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();

		assertEquals(Long.valueOf(simulationTime), time);
		assertEquals(Double.valueOf(predictionError), error);

		Double weightedError = evaluator.calculateWeightedPredictionError(shapeFile, perimeter1File,
				Long.valueOf(totalSimulationTime));
		// System.out.printf("%s Weighted Error: %s \n", shapeFilePath, weightedError);
		assertEquals(Double.valueOf(fitnessEvaluation), weightedError);
	}

}