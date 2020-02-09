package com.edigley.tsp.executors;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.ProcessUtil;
import com.edigley.tsp.util.shapefile.ShapeFileCalculator;

public class FarsiteExecutionMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutionMonitor.class);
	
	private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference());
	
	private static final NormalizedSymmetricDifference comparator = new NormalizedSymmetricDifference();
	
	public static void release() {
		executorService.shutdownNow();
		scheduledExecutorService.shutdownNow();
	}
	
	public static void monitorFarsiteExecution(long generation, long id, FarsiteIndividual individual, ScenarioProperties scenarioProperties, Process process) {
		
		executorService.execute(() -> {
	
			Long maxSimulatedTime = 0L;
			Double fireError = Double.MAX_VALUE;
			Double lastFireError = Double.valueOf(fireError);
			int nOfRepetitionsOfLastFireError = 0;
			
			long simulatedTime = scenarioProperties.getTimeToBeSimulated();			

			try {
				while ( (maxSimulatedTime < simulatedTime) && process.isAlive()) {
					
					TimeUnit.SECONDS.sleep(30);
					
					if (!process.isAlive()) {
						break;
					}
				
					File predictionFile = scenarioProperties.getShapeFileOutput(generation, id); 
					File perimeterFile = scenarioProperties.getPerimeterAtT1();
					
					//Pair<Long, Double> fireEvolution = FarsiteOutputProcessor.getInstance().getFireEvolution(gAFile, gBFile);
					//Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(predictionFile, perimeterFile, comparator);
					Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(predictionFile, perimeterFile);
					
					maxSimulatedTime = fireEvolution.getKey();
					fireError = fireEvolution.getValue();
					
					if (fireError.equals(lastFireError)) {
						nOfRepetitionsOfLastFireError++;
					} else {
						nOfRepetitionsOfLastFireError = 0;
						lastFireError = Double.valueOf(fireError);
					}
					
					logger.info(String.format("Individual [ %s %s ] %s -> Fire Evolution [ %s ] repeated < %s > times", generation, id, individual, fireEvolution, nOfRepetitionsOfLastFireError));
					
					if (mustKillFarsiteIndividual(predictionFile, scenarioProperties, fireError, nOfRepetitionsOfLastFireError)) {
						logger.info(String.format("Going to kill individual [ %s %s ] %s - with maxSimulatedTime = %s, fireError = %s ...\n", generation, id, individual, maxSimulatedTime, fireError));
						ProcessUtil.killAllDescendants(process);
						process.destroyForcibly();
						logger.info(String.format("Killed individual [ %s %s ] %s - with maxSimulatedTime = %s, fireError = %s \n", generation, id, individual, maxSimulatedTime, fireError));
					};
				}

				logger.info(String.format("Finish monitoring for individual [ %s %s ] %s - with maxSimulatedTime = %s, fireError = %s \n", generation, id, individual, maxSimulatedTime, fireError));
	
				if (process.isAlive()) {
					ProcessUtil.killAllDescendants(process);
					process.destroyForcibly();					
				}
			} catch (InterruptedException | RuntimeException e) {
				logger.error(String.format("Couldn't monitor individual [ %2s %3s ] %s - %s", generation, id, individual, e.getMessage()), e);
			} catch (IOException e) {
				logger.warn(String.format("Couldn't [ extract maximum simulated time / calculate fireError ] for individual  [ %2s %3s ] %s - ", generation, id, individual), e);
			}

		});
	}

	private static boolean mustKillFarsiteIndividual(File firePerimeter, ScenarioProperties scenarioProperties, Double fireError, int nOfRepetitionsOfLastFireError) throws IOException {
		Double maxTolerablePredictionError = scenarioProperties.getMaxTolerablePredictionError();
		Long maxNonProgressingIterations = scenarioProperties.getMaxNonProgressingIterations();
		return (Double.compare(fireError, maxTolerablePredictionError) > 0) 
			|| (nOfRepetitionsOfLastFireError > maxNonProgressingIterations) 
			|| (firePerimeterReachesLandscapeExtent(firePerimeter, scenarioProperties));
	}

	private static boolean firePerimeterReachesLandscapeExtent(File firePerimeter, ScenarioProperties scenarioProperties) throws IOException {
		File layerExtentFile = scenarioProperties.getLandscapeLayerExtentFile();
		return ShapeFileCalculator.boundariesTouch(firePerimeter, layerExtentFile);
	}

	@SuppressWarnings("unused")
	public static void monitorFarsiteExecutionImproved(long generation, long id, FarsiteIndividual individual, ScenarioProperties scenarioProperties) {
		
		Callable<Long> callable = new Callable<Long>() {
			public Long call() throws Exception {
				return new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference()).getSimulatedTime(scenarioProperties.getShapeFileOutput(generation, id));
			}
		};
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Long maxSimulatedTime = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference()).getSimulatedTime(scenarioProperties.getShapeFileOutput(generation, id));
					System.out.println("---> maxSimulatedTime: " + maxSimulatedTime);
					logger.info("---> maxSimulatedTime: " + maxSimulatedTime);
				} catch (Exception e) {
					System.err.printf("Couldn't extract maximum simulated time for individual %s - %s\n", individual, e.getMessage());
					logger.warn("Couldn't extract maximum simulated time for inidividual " + individual, e);
				}
			}
		};
		
		//ScheduledFuture<Double> scheduledFuture = scheduledExecutorService.schedule(callable, 10, TimeUnit.SECONDS);
		ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(runnable, 60L, 10L, TimeUnit.SECONDS);
		
		try {
			//scheduledFuture.get(60, TimeUnit.SECONDS);
			Double maxSimulatedTime = Double.valueOf(0);
			while (!maxSimulatedTime.equals(Double.valueOf(480))) {
				TimeUnit.SECONDS.sleep(10);
				//maxSimulatedTime 
				Object x = scheduledFuture.get();
				System.out.println("---> maxSimulatedTime: " + maxSimulatedTime);
				logger.info("---> maxSimulatedTime: " + maxSimulatedTime);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scheduledFuture.cancel(true);
		}
		
	}

	public static void main(String[] args) {
		Double zero = Double.valueOf(0);
		Double _480 = Double.valueOf(480);
		System.out.println(zero.equals(_480));
		System.out.println(zero.equals(Double.valueOf(480)));
		System.out.println(Double.valueOf(480).equals(_480));
		System.out.println(_480.equals(Double.valueOf(480)));
		System.out.println(Double.compare(zero, Double.valueOf(480)) < 0);
		System.out.println(Double.compare(_480, Double.valueOf(480)) < 0);
	}
	
}
