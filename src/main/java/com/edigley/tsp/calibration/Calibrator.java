package com.edigley.tsp.calibration;

import static com.edigley.tsp.ui.CLI.EVALUATION_FUNCTION;
import static com.edigley.tsp.ui.CLI.FARSITE;
import static com.edigley.tsp.ui.CLI.MEMOIZATION;
import static com.edigley.tsp.ui.CLI.PARALLELIZATION_LEVEL;
import static com.edigley.tsp.ui.CLI.SCENARIO_CONFIGURATION;
import static com.edigley.tsp.ui.CLI.SEED;
import static com.edigley.tsp.ui.CLI.TIME_OUT;
import static com.edigley.tsp.util.CLIUtils.assertsFilesExist;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.AdjustedGoodnessOfFit;
import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutionMonitor;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;

import io.jenetics.Optimize;

public class Calibrator {

	private static final Logger logger = LoggerFactory.getLogger(Calibrator.class);
	private static transient String msg;
	
	private CommandLine cmd;
	
	private File farsiteFile;
	
	private File scenarioDir;
	
	private ScenarioProperties scenarioProperties; 
	
	private GeneticAlgorithm geneticAlgorithm;
	
	private List<FarsiteExecution> results;

	// auxiliary flags
	private boolean prepared = false;
	private boolean finished = false;
	
	public Calibrator(CommandLine cmd) {
		this.cmd = cmd;
	}

	public void prepare() throws java.text.ParseException, IOException, ParseException, NoSuchAlgorithmException {
		farsiteFile = (File) cmd.getParsedOptionValue(FARSITE);
		scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		
		assertsFilesExist(farsiteFile, scenarioDir, new File(scenarioDir, ScenarioProperties.SCENARIO_FILE_NAME));
		
		scenarioProperties = new ScenarioProperties(scenarioDir);

		geneticAlgorithm = new GeneticAlgorithm(scenarioProperties, (Long) cmd.getParsedOptionValue(SEED));
		
		Long farsiteExecutionTimeOut = null;
		if (cmd.hasOption(TIME_OUT)) {
			farsiteExecutionTimeOut = (Long) cmd.getParsedOptionValue(TIME_OUT);
		} else {
			farsiteExecutionTimeOut = scenarioProperties.getFarsiteExecutionTimeout();
		}
		
		Long farsiteExecutionParallelizationLevel = null;
		if (cmd.hasOption(PARALLELIZATION_LEVEL)) {
			farsiteExecutionParallelizationLevel = (Long) cmd.getParsedOptionValue(PARALLELIZATION_LEVEL);
		} else {
			farsiteExecutionParallelizationLevel = scenarioProperties.getFarsiteParallelizationLevel();
		}
		
		ComparisonMethod comparator = null;
		Optimize optimizationStrategy = Optimize.MAXIMUM;
		if (cmd.hasOption(EVALUATION_FUNCTION)) {
			String evaluationFunction = cmd.getOptionValue(EVALUATION_FUNCTION);
			if (evaluationFunction.equals("gof")) {
				comparator = new GoodnessOfFit();
			} else if (evaluationFunction.equals("agof")) {
				comparator = new AdjustedGoodnessOfFit();
			} else if (evaluationFunction.equals("nsd")) {
				comparator = new NormalizedSymmetricDifference();
				optimizationStrategy = Optimize.MINIMUM;
			} else {
				throw new NoSuchAlgorithmException("There was no evaluator for '" + evaluationFunction + "'.");
			}
		} else {
			comparator = new NormalizedSymmetricDifference();
			optimizationStrategy = Optimize.MINIMUM;
		}
		
		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir, farsiteExecutionTimeOut, farsiteExecutionParallelizationLevel);
		executor.setScenarioProperties(scenarioProperties);
		FarsiteIndividualEvaluator evaluator = FarsiteIndividualEvaluator.getInstance();
		evaluator.setComparator(comparator);
		executor.setFitnessEvaluator(evaluator);
		geneticAlgorithm.setExecutor(executor);
		geneticAlgorithm.setOptimizationStrategy(optimizationStrategy);
		
		FarsiteExecutionMemoization cache;
		if (cmd.hasOption(MEMOIZATION)) {
			cache = new FarsiteExecutionMemoization(((File) cmd.getParsedOptionValue(MEMOIZATION)));
		} else {
			cache = new FarsiteExecutionMemoization(new File("farsite_execution_memoization_default.txt"));
		}
		
		geneticAlgorithm.setFarsiteExecutionCache(cache);
		
		prepared = true;
		
	}
	
	public void run() throws java.text.ParseException, IOException, ParseException, NoSuchAlgorithmException {
		assert !finished;
		
		if (!prepared) {
			prepare();			
		}
		
		this.results = geneticAlgorithm.run();
		
		finished = true;
	}

	public void printSummaryStatistics(StopWatch stopWatch) {
		msg = String.format("Genetic Algorithm - Best Calibrated Result: %s", results.get(0));
        logger.info(msg);System.out.println(msg);
	}

	public void releaseResources() {
		FarsitePopulationEvaluator.getInstance().release();
		FarsiteExecutionMonitor.release();
	}

}
