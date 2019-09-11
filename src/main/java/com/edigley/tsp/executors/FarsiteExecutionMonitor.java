package com.edigley.tsp.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.CallableBackgroundInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.input.ScenarioProperties;
import com.edigley.tsp.input.ShapeFileUtil;

public class FarsiteExecutionMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutionMonitor.class);
	
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	
	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);
	
	public static void release() {
		executorService.shutdownNow();
		scheduledExecutorService.shutdownNow();
	}
	
	public static void monitorFarsiteExecution(long generation, long id, FarsiteIndividual individual, ScenarioProperties scenarioProperties) {
		
		executorService.execute(() -> {
	
			Double maxSimulatedTime = Double.valueOf(0);
			
			long simulatedTime = scenarioProperties.getSimulatedTime();
			System.out.println("---> simulatedTime: " + simulatedTime);
			
			while (!maxSimulatedTime.equals(Double.valueOf(simulatedTime))) {
				
				try {
					
					TimeUnit.SECONDS.sleep(10);
				
					maxSimulatedTime = ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
					
					System.out.println("---> maxSimulatedTime: " + maxSimulatedTime);
					logger.info("---> maxSimulatedTime: " + maxSimulatedTime);
				} catch (Exception e) {
					System.err.printf("Couldn't extract maximum simulated time for individual %s - %s\n", individual, e.getMessage());
					logger.warn("Couldn't extract maximum simulated time for inidividual " + individual, e);
					//throw new RuntimeException(e);
				}
			}
			
			System.out.printf("Finish Monitoring for individual %s - with maxSimulatedTime %s \n", individual, maxSimulatedTime);

		});
	}

	public static void monitorFarsiteExecutionImproved(long generation, long id, FarsiteIndividual individual, ScenarioProperties scenarioProperties) {
		
		Callable<Double> callable = new Callable<Double>() {
			public Double call() throws Exception {
				return ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
			}
		};
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					Double maxSimulatedTime = ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
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
