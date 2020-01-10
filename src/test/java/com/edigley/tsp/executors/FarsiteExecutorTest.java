package com.edigley.tsp.executors;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;

public class FarsiteExecutorTest {

	private File farsiteFile = new File("target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction"); 
	private File scenarioDir = new File("playpen/fire-scenarios/jonquera/");
	private long generation = 9;
	private Long timeout = 300L;
	private Long parallelizationLevel = 1L;
	private ScenarioProperties scenarioProperties;
	private FarsiteExecutor executor;
	
	@Before
	public void setUp() throws FileNotFoundException, IOException {
		scenarioProperties = new ScenarioProperties(scenarioDir);
		executor = new FarsiteExecutor(farsiteFile, scenarioDir, timeout, parallelizationLevel);
		executor.setScenarioProperties(scenarioProperties);
	}
	
	@Test
	public void testRunFarsiteIndividualForNSD() throws Exception {
		NormalizedSymmetricDifference nsdComparator = new NormalizedSymmetricDifference();
		assertFarsiteExecution(nsdComparator, 10, "  9  12  14  22  87   165  353  38  50  1.7 ", 210, 0.9184562678635393, 2.099329);
		assertFarsiteExecution(nsdComparator, 11, "  6   7  14  37  79    53  350  31  96  1.5 ", 180, 0.9184562678635393, 2.439559);
	}

	@Test
	public void testRunFarsiteIndividualForGoF() throws Exception {
		GoodnessOfFit gofComparator = new GoodnessOfFit();
		assertFarsiteExecution(gofComparator, 20, "  15   5  13  51  93   10  354  37 100  0.7 ", 480, 0.325292, 0.325292);
		assertFarsiteExecution(gofComparator, 21, "  15  14   9  59  93   10  354  34 100  0.7 ", 480, 0.203161, 0.203161);
	}
	
	private void assertFarsiteExecution(ComparisonMethod comparator, int individualId, String individualAsString, int expectedSimulatedTime, Double expectedDifference, Double expectedWeightedError) throws Exception {
		
		FarsiteIndividual individual = new FarsiteIndividual(individualAsString);
		
		FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(comparator);
		executor.setFitnessEvaluator(evaluator);

		FarsiteExecution execution = executor.run(generation, individualId, individual);
		
		File perimeter1File = scenarioProperties.getPerimeterAtT1();
		
		File predictionFile = scenarioProperties.getShapeFileOutput(generation, individualId);
		
		Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(predictionFile, perimeter1File);
		Long simulatedTime = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();
		
		long timeToBeSimulated = scenarioProperties.getTimeToBeSimulated();
		Double weightedError = evaluator.calculateWeightedPredictionError(predictionFile, perimeter1File, timeToBeSimulated);
				
		String output = String.format("%s %s %s ", execution, timeToBeSimulated, expectedDifference);
		
		String expectedOutput = String.format("%s %s %s %s %s %s %s %s ", 
				individualAsString, 
				expectedWeightedError, 
				expectedSimulatedTime, 
				parallelizationLevel,
				execution.getExecutionTime(), 
				predictionFile.getName(),
				timeToBeSimulated, 
				expectedDifference);
		
		assertEquals(removeDoubleSpaces(expectedOutput), removeDoubleSpaces(output));
		
	}
	
	private static String removeDoubleSpaces(String s) {
		return s.replace("  ", " ").replace("  ", " ").replace("  ", " ").replace(",", ".");
	}
	
}