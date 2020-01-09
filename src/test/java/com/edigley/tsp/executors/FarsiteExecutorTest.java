package com.edigley.tsp.executors;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;

public class FarsiteExecutorTest {

	@Test
	public void testRunFarsiteIndividual() throws Exception {
		//assertFarsiteExecution("  9  12  14  22  87   165  353  38  50  1.7 ", 180, 0.9184562678635393, 2.099329);
		assertFarsiteExecution("  6   7  14  37  79    53  350  31  96  1.5 ", 180, 0.9184562678635393, 2.439559);
	}
	
	private static void assertFarsiteExecution(String individualAsString, int expectedSimulatedTime, Double expectedNormalizedSymmetricDifference, Double expectedWeightedError) throws Exception {
		
		FarsiteIndividual individual = new FarsiteIndividual(individualAsString);
		
		File farsiteFile = new File("target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction"); 
		File scenarioDir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/");
		
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		
		Long timeout = 300L;
		Long parallelizationLevel = 1L;
		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir, timeout, parallelizationLevel);
		executor.setScenarioProperties(scenarioProperties);
		FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference());
		executor.setFitnessEvaluator(evaluator);

		long generation = 9;
		long individualId = 9;
		FarsiteExecution execution = executor.run(generation, individualId, individual);
		
		FarsiteExecutionMonitor.release();
		
		File perimeter1File = scenarioProperties.getPerimeterAtT1();
		
		File predictionFile = scenarioProperties.getShapeFileOutput(generation, individualId);
		
		Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(predictionFile, perimeter1File);
		Long simulatedTime = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();
		
		long timeToBeSimulated = scenarioProperties.getTimeToBeSimulated();
		Double weightedError = evaluator.calculateWeightedPredictionError(predictionFile, perimeter1File, timeToBeSimulated);
				
		String output = String.format("%s %s %s ", execution, timeToBeSimulated, expectedNormalizedSymmetricDifference);
		
		//1 t10 t100 t1000 t10000 ws wd th hh adj fireError maxSimulatedTime parallelizationLevel executionTime timeToBeSimulated, expectedNormalizedSymmetricDifference
		System.out.printf("Header:     %s timeToBeSimulated  expectedNormalizedSymmetricDifference \n", FarsiteExecution.header);
		System.out.printf("Execution: %s \n", output);
		
		String expectedOutput = String.format("%s %s %s 1 %s %s %s ", individualAsString, expectedWeightedError, expectedSimulatedTime, execution.getExecutionTime(), timeToBeSimulated, expectedNormalizedSymmetricDifference);
		assertEquals(removeDoubleSpaces(output), removeDoubleSpaces(expectedOutput));

	}
	
	private static String removeDoubleSpaces(String s) {
		return s.replace("  ", " ").replace("  ", " ").replace("  ", " ").replace(",", ".");
	}
	
}