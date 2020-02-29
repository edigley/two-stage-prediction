package com.edigley.tsp.ui;

import static com.edigley.tsp.util.CLIUtils.assertsFilesExist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.edigley.tsp.comparator.AdjustedGoodnessOfFit;
import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.io.input.ScenarioProperties;

import io.jenetics.Optimize;

import static com.edigley.tsp.ui.CLI.*;

public class CommandLineInterpreter {

	public static Options prepareOptions() {
		Options options = new Options();
		Option executable = new Option(FARSITE, "farsite", true, "Farsite executable file path");
		Option scenario = new Option(SCENARIO_CONFIGURATION, "scenario", true, "Scenario directory path");
		Option memoization = new Option(MEMOIZATION, "memoization", true, "Memoization file path");
		Option timeOut = new Option(TIME_OUT, "timeout", true, "Timeout for each individual execution");
		Option seed = new Option(SEED, "seed", true, "Seed to be used when generating the initial population");
		Option parallelizationLevel = new Option(PARALLELIZATION_LEVEL, "parallelization_level", true, "Set the nivel of parallelization in number of threads of an individual execution");
		Option evaluationFunction = new Option(EVALUATION_FUNCTION, "evaluation_function", true, "Evaluation Function");
		
		Option recalculate = new Option(RECALCULATE, "recalculate_memoization", false, "Should recalculate memoization fire erros");
		Option compare = new Option(COMPARE, "compare_files", false, "Should compare prediction and real fire expansion files");
		Option calibrate = new Option(CALIBRATE, "calibrate_input", false, "Should calibrate inputs to prediction");
		Option predict = new Option(PREDICT, "predict_fire_spread", false, "Should run prediction");
		Option bestIndividuals = new Option(BEST_INDIVIDUALS_FILE, "best_individuals_file", true, "File with individuals to be used in the prediction phase");
		
		Option prediction = new Option(PREDICTION_FILE, "prediction_file", true, "Prediction file path");
		Option real = new Option(PERIMETER_FILE, "real_file", true, "Real expansion file path");
		Option layer = new Option(LAYER_EXTENT_FILE, "layer_extent_file", true, "Layer extent file path");
		
		/* set args mandatoriness */
		
		//mandatory args
		executable.setRequired(true);
		scenario.setRequired(true);
		
		//non mandatory args
		memoization.setRequired(false);
		timeOut.setRequired(false);
		seed.setRequired(false);
		parallelizationLevel.setRequired(false);
		evaluationFunction.setRequired(false);
		recalculate.setRequired(false);
		calibrate.setRequired(false);
		predict.setRequired(false);
		bestIndividuals.setRequired(false);
		
		/* set args type */
		
		executable.setType(File.class);
		scenario.setType(File.class);
		memoization.setType(File.class);
		bestIndividuals.setType(File.class);
		
		timeOut.setType(Number.class);
		seed.setType(Number.class);
		parallelizationLevel.setType(Number.class);
		
		evaluationFunction.setType(String.class);
		
		prediction.setType(File.class);
		real.setType(File.class);
		layer.setType(File.class);
		
		/* add options */
		
		options.addOption(executable);
		options.addOption(scenario);
		options.addOption(memoization);
		options.addOption(timeOut);
		options.addOption(seed);
		options.addOption(parallelizationLevel);
		options.addOption(evaluationFunction);
		options.addOption(recalculate);
		options.addOption(compare);
		options.addOption(calibrate);
		options.addOption(predict);
		options.addOption(bestIndividuals);
		options.addOption(prediction);
		options.addOption(real);
		options.addOption(layer);
		
		return options;
	}
	
	public static FarsiteExecutor prepareFarsiteExecutor(CommandLine cmd) throws ParseException, FileNotFoundException, IOException {
		File farsiteFile = (File) cmd.getParsedOptionValue(FARSITE);
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);	
		
		assertsFilesExist(farsiteFile, scenarioDir, new File(scenarioDir, ScenarioProperties.SCENARIO_FILE_NAME));
		
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);

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

		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir, farsiteExecutionTimeOut, farsiteExecutionParallelizationLevel);
		executor.setScenarioProperties(scenarioProperties);
		return executor;
	}
	
	public static Pair<ComparisonMethod, Optimize> defineComparisonCriteria(CommandLine cmd) throws NoSuchAlgorithmException {
		ComparisonMethod comparator = null;
		Optimize optimizationStrategy = null;
		
		if (cmd.hasOption(EVALUATION_FUNCTION)) {
			String evaluationFunction = cmd.getOptionValue(EVALUATION_FUNCTION);
			if (evaluationFunction.equals("gof")) {
				comparator = new GoodnessOfFit();
				optimizationStrategy = Optimize.MAXIMUM;
			} else if (evaluationFunction.equals("agof")) {
				comparator = new AdjustedGoodnessOfFit();
				optimizationStrategy = Optimize.MAXIMUM;
			} else if (evaluationFunction.equals("nsd")) {
				comparator = new NormalizedSymmetricDifference();
				optimizationStrategy = Optimize.MINIMUM;
			} else {
				throw new NoSuchAlgorithmException("There was no evaluator for comparison method '" + evaluationFunction + "'.");
			}
		} else {
			comparator = new AdjustedGoodnessOfFit();
			optimizationStrategy = Optimize.MAXIMUM;
		}
		
		Pair<ComparisonMethod, Optimize> comparisonCriteria = ImmutablePair.of(comparator, optimizationStrategy);
		return comparisonCriteria;
	}

	public static FarsiteExecutionMemoization defineMemoizationCache(CommandLine cmd) throws ParseException {
		FarsiteExecutionMemoization cache;
		if (cmd.hasOption(MEMOIZATION)) {
			cache = new FarsiteExecutionMemoization(((File) cmd.getParsedOptionValue(MEMOIZATION)));
		} else {
			cache = new FarsiteExecutionMemoization(new File("farsite_execution_memoization_default.txt"));
		}
		return cache;
	}
	
}