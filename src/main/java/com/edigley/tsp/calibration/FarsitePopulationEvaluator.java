package com.edigley.tsp.calibration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.executors.FarsiteIndividual;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

public class FarsitePopulationEvaluator implements Evaluator<IntegerGene, Double> {

	private static final Logger logger = LoggerFactory.getLogger(FarsitePopulationEvaluator.class);
	private static transient String msg;
	
	private static AtomicInteger nOfEvaluationCallsCount = new AtomicInteger(1);
	private int nOfEvaluationCalls;
	
	private static FarsiteExecutionMemoization cache;
	
	private static FarsiteExecutor executor;
	
	public static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static final FarsitePopulationEvaluator instance = new FarsitePopulationEvaluator();
	
	private FarsitePopulationEvaluator() { 
	}
	
	public static FarsitePopulationEvaluator getInstance() {
		return instance;
	}
	
	public static void setCache(FarsiteExecutionMemoization theCache) {
		cache = theCache;
	}
	
	public static void setFarsiteExecutor(FarsiteExecutor theExecutor) {
		executor = theExecutor;
	}
	
	@Override
	public ISeq<Phenotype<IntegerGene, Double>> eval(Seq<Phenotype<IntegerGene, Double>> population) {
		
		nOfEvaluationCalls = nOfEvaluationCallsCount.getAndIncrement();
		
		populationSummary("Start ", nOfEvaluationCalls, population);
		
		Long nOfNonEvaluated = population.stream().filter(Phenotype::nonEvaluated).count();
		CountDownLatch latch = new CountDownLatch(nOfNonEvaluated.intValue());
		
		population.stream()
			.filter(Phenotype::nonEvaluated)
			.forEach(
				p -> {
					final String individual = FarsiteIndividual.toStringParams(p.getGenotype());
					logger.debug(" ---> " + individual + " " + p.isEvaluated());
					executorService.submit(() -> { p.eval(FarsitePopulationEvaluator::eval); latch.countDown(); });
				}
			);	
		
		try {
			String msg = "Going to wait for all the " + nOfNonEvaluated + " threads in order to finish...";
			logger.debug(msg);System.out.println(msg);
			latch.await(executor.getTimeout()*3, TimeUnit.SECONDS);
			//latch.await();
			msg = "All threads have been finished successfully.";
			logger.debug(msg);System.out.println(msg);
		} catch (InterruptedException e) {
			msg = "Error while expecting all tasks to finish";
			logger.error(msg, e);System.err.println(msg + " " + e.getMessage());
		}
		
		ISeq<Phenotype<IntegerGene, Double>> evaluatedPopulation = population
				.map( p -> 
					p.eval(	pt -> {
						FarsiteExecution farsiteExecution = cache.get(new FarsiteIndividual(pt));
						return farsiteExecution != null ? farsiteExecution.getFireError() : null;
					})
				)
				.asISeq();
		populationSummary("Finish", nOfEvaluationCalls, evaluatedPopulation);
		System.out.println("");
		return evaluatedPopulation;
	}

	private static Double eval(Genotype<IntegerGene> gt) {
		
		Thread currentThread = Thread.currentThread();
		
     	int id = GeneticAlgorithm.atomicCount.incrementAndGet();
     	FarsiteIndividual individual = new FarsiteIndividual(gt);
     	logger.debug(String.format("Going to check cached value for individual %s", individual));
     	FarsiteExecution cachedExecution = cache.get(individual);
     	logger.debug(String.format("Cached value for individual %s: %s", individual, cachedExecution));
		if (cachedExecution != null) {
			Double error = null;
			if (cachedExecution.getMaxSimulatedTime().equals(Double.valueOf(480))) {
				String msg = String.format("%2s %3s %s -> [%s] - CACHED", GeneticAlgorithm.generation, id, cachedExecution, currentThread.getName());
				logger.info(msg);System.out.println(msg);
				error = cachedExecution.getFireError();
			} else {
				String msg = String.format("%2s %3s %s -> [%s] - NaN", GeneticAlgorithm.generation, id, cachedExecution, currentThread.getName());
				logger.info(msg);System.out.println(msg);
				error = Double.NaN;
			}
			return error;
     	} else {
     		String msg = String.format("Execution  started: %2s %3s %s -> [%s]", GeneticAlgorithm.generation, id, individual, currentThread.getName());
     		logger.info(msg);//System.out.println(msg);
			FarsiteExecution execution = executor.run(GeneticAlgorithm.generation, id, individual);
			cache.add(execution);
			msg = String.format("Execution finished: %2s %3s %s -> [%s]", GeneticAlgorithm.generation, id, execution, currentThread.getName());
			logger.info(msg);System.out.println(msg);
	    	return execution.getFireError();
     	}
    }

	public void release() {
		msg = "Going to shutdown the thread pool...";
		logger.debug(msg);System.out.println(msg);
		executorService.shutdownNow();
		try {
			msg = "Going to wait for all the threads in order to finish...";
			logger.debug(msg);System.out.println(msg);
			executorService.awaitTermination(60, TimeUnit.SECONDS);
			msg = "All threads have been finished successfully.";
			logger.debug(msg);System.out.println(msg);
		} catch (InterruptedException e) {
			msg = "Error when trying to shutdown executor service";
			logger.info(msg, e);System.err.println(msg + " " + e.getMessage());
		}
	}

	private void populationSummary(String position, int nOfEvaluationCalls, Seq<Phenotype<IntegerGene, Double>> population) {
		
		long nPhenEvaluated = population.stream().filter(Phenotype::isEvaluated).count();
		Long maxPhenGeneration = population.stream().mapToLong(Phenotype::getGeneration).max().orElse(0L);
		int popSize = population.size();
		
		String pattern = "Evaluation.%s -->  %s : %s / %s / %s / %s / %s <---------";
		String header = "#Evaluation Calls / GA.generation / maxPhenGeneration / popSize / nPhenEvaluated";
		
		msg = String.format(pattern, position, header, nOfEvaluationCalls, GeneticAlgorithm.generation, maxPhenGeneration, popSize, nPhenEvaluated);
		
		logger.debug(msg);System.out.println(msg);
	}

}