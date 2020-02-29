package com.edigley.tsp.ui;

import static com.edigley.tsp.util.CLIUtils.assertsFilesExist;
import static com.edigley.tsp.util.CLIUtils.parseCommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.calibration.Calibrator;
import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.io.output.FarsiteOutputSaver;
import com.edigley.tsp.util.ErrorCode;
import com.edigley.tsp.util.ExecutorServiceUtil;

import io.jenetics.Optimize;

public class CLI {

	private static final Logger logger = LoggerFactory.getLogger(CLI.class);
	
	public static final String FARSITE = "f";
	
	public static final String SCENARIO_CONFIGURATION = "c";
	
	public static final String MEMOIZATION = "m";

	public static final String TIME_OUT = "t";
	
	public static final String PARALLELIZATION_LEVEL = "p";
	
	public static final String EVALUATION_FUNCTION = "e";

	public static final String SEED = "s";
	
	public static final String CALIBRATE = "calibrate";
	
	public static final String PREDICT = "predict";
	
	public static final String RECALCULATE = "recalculate";
	
	public static final String COMPARE = "compare";
	
	public static final String PREDICTION_FILE = "prediction";
	
	public static final String BEST_INDIVIDUALS_FILE = "b";
	
	public static final String PERIMETER_FILE = "perimeter";
	
	public static final String LAYER_EXTENT_FILE = "layer";

	public static final String HELP = "help";

	public static final String USAGE = "usage";
	
	public static final String EXECUTION_LINE = "java -jar two-stage-prediction.jar";
	
	public static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static List<FarsiteExecution> runCalibration(CommandLine cmd) throws ParseException, java.text.ParseException, IOException, NoSuchAlgorithmException {
		logger.info("Going to start the calibration stage...");
		logger.info("ScenarioProperties.seed                         : " + (Long) cmd.getParsedOptionValue(SEED));
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Calibrator calibrator = new Calibrator(cmd);
		
		calibrator.prepare();
		
		List<FarsiteExecution> bestIndividuals = calibrator.run();

		stopWatch.stop();
		
		calibrator.printSummaryStatistics(stopWatch);
		
		//it is crucial to release the resources and then be able to finish the application
		calibrator.releaseResources();

		logger.info("Calibration finished.");
		
		return bestIndividuals;
	}

	private static void runPrediction(int generation, List<FarsiteExecution> bestIndividuals, FarsiteExecutor farsiteExecutor) throws ParseException, InterruptedException {
		logger.info("Going to start the prediction stage...");
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		CountDownLatch latch = new CountDownLatch(bestIndividuals.size());

		final AtomicInteger atomicIdCount = new AtomicInteger(0);
		
		bestIndividuals.stream()
			.map(b -> b.getIndividual())
			.forEach( individual -> {
				executorService.submit(() -> {
					try {
						farsiteExecutor.run(generation, atomicIdCount.incrementAndGet(), individual);
						String msg = String.format("Finished prediction for Individual -> %s ", individual);
						logger.info(msg);System.out.println(msg);
					} catch (Exception e) { 
						String msg = String.format("Prediction failed for Individual -> %s -> %s", individual, e.getMessage());
						logger.error(msg, e);System.err.println(msg); 
					} finally {
						latch.countDown();
					}
				});
			});

		latch.await(farsiteExecutor.getTimeout()*3, TimeUnit.SECONDS);
		
		stopWatch.stop();
		
		logger.info("Prediction finished.");
	}
	
	private static void recalculateFitnessForAllIndividuals(CommandLine cmd) throws ParseException {
		logger.info("Going to recalculate fitness function to all individuals in the memoization file...");
		
		File perimeterFile = (File) cmd.getParsedOptionValue(PERIMETER_FILE);
		File memoizationFile = (File) cmd.getParsedOptionValue(MEMOIZATION);
		
		FarsiteExecutionMemoization memoization = new FarsiteExecutionMemoization(memoizationFile);
		
		memoization.recalculateFireError(memoizationFile, perimeterFile);
		
		logger.info("Recalculation finished.");
	}

	private static File generatePredictionImageComparison(CommandLine cmd) throws ParseException, FileNotFoundException, IOException {
		logger.info("Going to generate a jpg image comparing prediction and actual fire perimeter...");
		
		File predictionFile = (File) cmd.getParsedOptionValue(PREDICTION_FILE);
		File perimeterFile = (File) cmd.getParsedOptionValue(PERIMETER_FILE);
		File layerExtentFile = (File) cmd.getParsedOptionValue(LAYER_EXTENT_FILE);

		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		
		assertsFilesExist(predictionFile, perimeterFile, layerExtentFile);
		
		File savedFile = FarsiteOutputSaver.saveAsJPG(perimeterFile, predictionFile, layerExtentFile, scenarioProperties);
		
		if (savedFile != null) {
			logger.info("JPG image successfully generated: " + savedFile.getAbsolutePath());				
		} else {
			logger.error("Couldn't save jpg file.");
		}
		
		return savedFile;
	}

	public static void main(String[] args) throws Exception {
		
		Locale.setDefault(new Locale("en", "US"));
		
		CommandLine cmd = parseCommandLine(args, CommandLineInterpreter.prepareOptions(), HELP, USAGE, EXECUTION_LINE);
		
		if (cmd.hasOption(COMPARE)) { // generates a jpg image comparing prediction and actual fire perimeter 
			generatePredictionImageComparison(cmd);
		} else if (cmd.hasOption(RECALCULATE)) { // recalculate fitness function to all individuals in the memoization file
			recalculateFitnessForAllIndividuals(cmd);
		} else { 
			
			List<FarsiteExecution> bestIndividuals = null;
			
			if (cmd.hasOption(CALIBRATE)) { // execution to calibrate and find the best individuals 
				bestIndividuals = runCalibration(cmd);
				saveBestIndividuals(cmd, bestIndividuals);
			}
			
			if (cmd.hasOption(PREDICT)) { // execution to predict fire spread

				FarsiteExecutor farsiteExecutor = defineFarsiteExecutor(cmd);
				
				if (bestIndividuals == null) {
					System.out.println("Going to run prediction for best individuals provided by the user");
					bestIndividuals = defineBestIndividuals(cmd);
				} else {
					System.out.println("Going to run prediction for calibrated best individuals");
				}

				int predictionGeneration = definePredictionGeneration(cmd);
				runPrediction(predictionGeneration, bestIndividuals, farsiteExecutor);
				
				ExecutorServiceUtil.release(executorService);
			}

			//farsite wrapper running
			//if (fireError.equals(Double.NaN) || fireError > 9999) {
			//	logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
			//Long maxSimulatedTime = evaluator.getSimulatedTime(scenarioProperties.getShapeFileOutput(generation, id));
			//Long maxSimulatedTime = evaluator.getSimulatedTime(predictionFile);
			//if (cachedExecution.getMaxSimulatedTime().equals(executor.getSimulatedTime())) {
			/*} else {
				String msg = String.format("%2s %3s %s -> [%s] - NaN", generation, individualId, cachedExecution, currentThread.getName());
				logger.info(msg);System.out.println(msg);
				error = Double.NaN;
			}*/
			//latch.await(executor.getTimeout()*3, TimeUnit.SECONDS);
			//latch.await();
		}
		
	}

	private static int definePredictionGeneration(CommandLine cmd) throws ParseException, FileNotFoundException, IOException {
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		int predictionGeneration = scenarioProperties.getNumGenerations() + 1;
		return predictionGeneration;
	}

	private static FarsiteExecutor defineFarsiteExecutor(CommandLine cmd)
			throws NoSuchAlgorithmException, ParseException, FileNotFoundException, IOException {
		Pair<ComparisonMethod, Optimize> comparisonCriteria = CommandLineInterpreter.defineComparisonCriteria(cmd);
		
		FarsiteExecutor farsiteExecutor = CommandLineInterpreter.prepareFarsiteExecutor(cmd);
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		farsiteExecutor.setScenarioProperties(scenarioProperties);

		ComparisonMethod comparator = comparisonCriteria.getLeft();
		Optimize optimizationStrategy = comparisonCriteria.getRight();
		
		comparator.setIgnitionPerimeterFile(scenarioProperties.getPerimeterAtT1File());
		
		FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(comparator);
		farsiteExecutor.setFitnessEvaluator(evaluator);
		return farsiteExecutor;
	}

	private static List<FarsiteExecution> defineBestIndividuals(CommandLine cmd)
			throws ParseException, FileNotFoundException {
		if (cmd.hasOption(BEST_INDIVIDUALS_FILE)) {
			File bestIndividualsFile = (File) cmd.getParsedOptionValue(BEST_INDIVIDUALS_FILE);
			assertsFilesExist(bestIndividualsFile);
			return readIndividualsFile(bestIndividualsFile);
			
		} else {
			System.err.println("No best individuals file was informed to run the prediction phase.");
			System.exit(ErrorCode.NONEXISTENT_BEST_INDIVIDUALS_FILE);
			return null;
		}
	}

	private static List<FarsiteExecution> readIndividualsFile(File bestIndividualsFile) throws FileNotFoundException {

		Scanner sc = new Scanner(bestIndividualsFile);
		String firstLine = sc.nextLine(); 
		
		if (!FarsiteExecution.header.equals(firstLine)) {
			String pattern = "First line from file %s doesn't contain expected header. Expected: %s. Actual: %s";
			String msg = String.format(pattern, bestIndividualsFile.getAbsolutePath(), FarsiteExecution.header, firstLine);
			logger.error(msg);System.err.println(msg);
			System.exit(ErrorCode.UNEXPECTED_MEMOIZATION_FILE_HEADER);
		}
		
		List<FarsiteExecution> bestIndividuals = new ArrayList<FarsiteExecution>();
		
		String nextLine;
		while (sc.hasNextLine() && !(nextLine=sc.nextLine()).trim().isEmpty()) {
			FarsiteExecution execution = new FarsiteExecution(nextLine);
			bestIndividuals.add(execution);
		}
		sc.close();
		
		return bestIndividuals;
	}

	private static void saveBestIndividuals(CommandLine cmd, List<FarsiteExecution> bestIndividuals) throws IOException, ParseException {
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		File bestIndividualsFile = scenarioProperties.getBestIndividualsFile();
		save(bestIndividualsFile, bestIndividuals);
	}
	
	private static void save(File outputFile, List<FarsiteExecution> executions) throws IOException {
		FileWriter fw = new FileWriter(outputFile, true);
		for (FarsiteExecution execution : executions) {
			fw.append(execution.toString() + "\n");
			fw.flush();
		}
		fw.close();
	}
	
}

