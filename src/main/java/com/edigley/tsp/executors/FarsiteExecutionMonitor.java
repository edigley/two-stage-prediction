package com.edigley.tsp.executors;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.input.ScenarioProperties;
import com.edigley.tsp.input.ShapeFileUtil;
import com.edigley.tsp.util.ProcessUtil;

public class FarsiteExecutionMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutionMonitor.class);
	
	private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	
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
			
			long simulatedTime = scenarioProperties.getSimulatedTime();
			
			while (!maxSimulatedTime.equals(simulatedTime) && process.isAlive()) {
				
				try {
					
					TimeUnit.SECONDS.sleep(30);
				
					File gAFile = scenarioProperties.getPerimeterAtT1();
					File gBFile = scenarioProperties.getOutputFile(generation, id); 
					
					Pair<Long, Double> fireEvolution = ShapeFileUtil.getFireEvolution(gAFile, gBFile);
					
					maxSimulatedTime = fireEvolution.getKey();
					fireError = fireEvolution.getValue();
					
					logger.info(String.format("Individual [ %s %s ] -> Fire Evolution [ %s ] repeated < %s > times ", generation, individual, fireEvolution, nOfRepetitionsOfLastFireError));
					
					if (fireError.equals(lastFireError)) {
						nOfRepetitionsOfLastFireError++;
					} else {
						nOfRepetitionsOfLastFireError = 0;
						lastFireError = Double.valueOf(fireError);
					}
					
					if (Double.compare(fireError, Double.valueOf(1.5)) > 0 || nOfRepetitionsOfLastFireError > 20) {
						ProcessUtil.killAllDescendants(process);
						process.destroyForcibly();
					};
					
				} catch (Exception e) {
					logger.warn("Couldn't extract maximum simulated time for inidividual: " + individual, e);
				}
			}
			
			logger.info(String.format("Finish monitoring for individual [%s] - with maxSimulatedTime = %s, fireError = %s \n", individual, maxSimulatedTime, fireError));

		});
	}

	public static void monitorFarsiteExecutionImproved(long generation, long id, FarsiteIndividual individual, ScenarioProperties scenarioProperties) {
		
		Callable<Long> callable = new Callable<Long>() {
			public Long call() throws Exception {
				return ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
			}
		};
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Long maxSimulatedTime = ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
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
