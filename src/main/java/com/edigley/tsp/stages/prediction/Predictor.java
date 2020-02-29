package com.edigley.tsp.stages.prediction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMonitor;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.stages.Stage;
import com.edigley.tsp.ui.CommandLineInterpreter;
import com.edigley.tsp.util.ExecutorServiceUtil;

import static com.edigley.tsp.ui.CLI.SCENARIO_CONFIGURATION;

import io.jenetics.Optimize;

public class Predictor extends Stage {

	private static final Logger logger = LoggerFactory.getLogger(Predictor.class);
	
	private long predictionTimeout; // TimeUnit.SECONDS
	
	private List<FarsiteExecution> bestIndividuals;
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private FarsiteExecutor farsiteExecutor; 

	public Predictor(CommandLine cmd, List<FarsiteExecution> bestIndividuals) {
		super(cmd);
		this.bestIndividuals = bestIndividuals;
	}

	@Override
	public boolean printSummaryStatistics(StopWatch stopWatch) {
		if (this.finished && this.results != null && !this.results.isEmpty()) {
			msg = "Prediction results:";
			logger.info(msg);System.out.println(msg);			
			this.results.stream().forEach(result -> {
				msg = String.format("%s", result);
				logger.info(msg);System.out.println(msg);
			});
			return true;
		} else {
			msg = String.format("Predictor: - No result to be printed because this predictor is not finished.");
			logger.warn(msg);System.err.println(msg);
			return false;
		}
	}

	@Override
	public void releaseResources() {
		FarsiteExecutionMonitor.release();
		ExecutorServiceUtil.release(executorService);
	}

	@Override
	public void prepare() throws ParseException, IOException, org.apache.commons.cli.ParseException, NoSuchAlgorithmException {
		this.farsiteExecutor = defineFarsiteExecutor(cmd);
		predictionTimeout = farsiteExecutor.getTimeout() * 3;
		this.results = new ArrayList<>();
		prepared = true;
	}

	@Override
	protected List<FarsiteExecution> execute() {
		
		CountDownLatch latch = new CountDownLatch(bestIndividuals.size());

		final AtomicInteger atomicIdCount = new AtomicInteger(0);
		
		bestIndividuals.stream()
			.map(b -> b.getIndividual())
			.forEach( individual -> {
				executorService.submit(() -> {
					try {
						int predictionGeneration = definePredictionGeneration(cmd);
						FarsiteExecution prediction = farsiteExecutor.run(predictionGeneration, atomicIdCount.incrementAndGet(), individual);
						this.results.add(prediction);
						msg = String.format("Finished prediction for Individual -> %s ", individual);
						logger.info(msg);System.out.println(msg);
					} catch (Exception e) { 
						msg = String.format("Prediction failed for Individual -> %s -> %s", individual, e.getMessage());
						logger.error(msg, e);System.err.println(msg); 
					} finally {
						latch.countDown();
					}
				});
			});

		try {
			latch.await(predictionTimeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			msg = String.format("Couldn't wait for all predictions: %s", e.getMessage());
			logger.error(msg, e);System.err.println(msg); 
		}
		
		return this.results;
	}
	
	private static FarsiteExecutor defineFarsiteExecutor(CommandLine cmd) throws NoSuchAlgorithmException, ParseException, FileNotFoundException, IOException, org.apache.commons.cli.ParseException {
		Pair<ComparisonMethod, Optimize> comparisonCriteria = CommandLineInterpreter.defineComparisonCriteria(cmd);
		
		FarsiteExecutor farsiteExecutor = CommandLineInterpreter.prepareFarsiteExecutor(cmd);
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		farsiteExecutor.setScenarioProperties(scenarioProperties);

		ComparisonMethod comparator = comparisonCriteria.getLeft();
		//Optimize optimizationStrategy = comparisonCriteria.getRight();
		
		comparator.setIgnitionPerimeterFile(scenarioProperties.getPerimeterAtT1File());
		
		FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(comparator);
		farsiteExecutor.setFitnessEvaluator(evaluator);
		return farsiteExecutor;
	}

	private static int definePredictionGeneration(CommandLine cmd) throws ParseException, FileNotFoundException, IOException, org.apache.commons.cli.ParseException {
		File scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO_CONFIGURATION);
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		int predictionGeneration = scenarioProperties.getNumGenerations() + 1;
		return predictionGeneration;
	}

}
