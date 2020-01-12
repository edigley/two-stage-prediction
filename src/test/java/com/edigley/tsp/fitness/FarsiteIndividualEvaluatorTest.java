package com.edigley.tsp.fitness;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;

public class FarsiteIndividualEvaluatorTest {

	private File resourcesDir = new File("src/test/resources/");

	private FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(new GoodnessOfFit());

	@Test
	public void testCalculatePredictionGoF() throws Exception {
		evaluator.setComparator(new GoodnessOfFit());
		assertPrediction("shape_1_1.shp", 180, 0.27865477309465514, 480, 0.104496);
		assertPrediction("shape_1_3.shp", 180, 0.3851732212437519, 480, 0.14444);
	}

	@Test
	public void testCalculatePredictionError() throws Exception {
		evaluator.setComparator(new NormalizedSymmetricDifference());
		assertPrediction("shape_1_3.shp", 180, 0.9148345582325907, 480, 2.439559);
	}

	private void assertPrediction(String predictionFilePath, int simulationTime, double prediction,
			int totalSimulationTime, double fitnessEvaluation) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, predictionFilePath);

		Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(shapeFile, perimeter1File);

		Long simulatedTime = fireEvolution.getLeft();
		Double predictionFitness = fireEvolution.getRight();

		assertEquals(Long.valueOf(simulationTime), simulatedTime);
		assertEquals(Double.valueOf(prediction), predictionFitness);

		Double weightedError = evaluator.calculateWeightedPredictionError(shapeFile, perimeter1File,
				Long.valueOf(totalSimulationTime));
		assertEquals(Double.valueOf(fitnessEvaluation), weightedError);
	}
	
}