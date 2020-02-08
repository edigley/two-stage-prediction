package com.edigley.tsp.calibration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;

import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

public class FarsitePopulationEvaluator implements Evaluator<IntegerGene, Double> {

	private static final Logger logger = LoggerFactory.getLogger(FarsitePopulationEvaluator.class);
	private static transient String msg;
	
	private static AtomicInteger nOfEvaluationCallsCount = new AtomicInteger(0);
	private int nOfEvaluationCalls;
	
	private static AtomicInteger executionIdCount = new AtomicInteger(0);
	private static AtomicInteger executionIdPerGenerationCount = new AtomicInteger(0);
	
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
		
		executionIdPerGenerationCount.set(0);
		
		nOfEvaluationCalls = nOfEvaluationCallsCount.incrementAndGet();
		
		populationSummary("Start ", nOfEvaluationCalls, population);
		
		Long nOfNonEvaluated = population.stream().filter(Phenotype::nonEvaluated).count();
		CountDownLatch latch = new CountDownLatch(nOfNonEvaluated.intValue());
		
		population.stream()
			.filter(Phenotype::nonEvaluated)
			.forEach(
				p -> {
					final String individual = FarsiteIndividual.toStringParams(p.getGenotype());
					executorService.submit(() -> {
						try {
							p.eval(FarsitePopulationEvaluator::eval); 
						} catch (Exception e) { 
							String msg = String.format("Individual evaluation failed -> %s -> %s", individual, e.getMessage());
							logger.error(msg, e);System.err.println(msg); 
						} finally {
							latch.countDown();
						}
					});
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
		
		System.out.println("Going to consolidate all fitness results");
		logger.debug("Going to consolidate all fitness results");
		ISeq<Phenotype<IntegerGene, Double>> evaluatedPopulation = population
				.map( p -> 
					p.eval(	gt -> cache.getFireError(new FarsiteIndividual(gt)) )
				)
				.asISeq();
		populationSummary("Finish", nOfEvaluationCalls, evaluatedPopulation);
		System.out.println("");
		return evaluatedPopulation;
	}

	private static Double eval(Genotype<IntegerGene> gt) {
		
     	int id = GeneticAlgorithm.atomicCount.incrementAndGet();
     	
     	FarsiteIndividual individual = new FarsiteIndividual(gt);
     	logger.debug(String.format("Going to evaluate individual: %s", individual));
     	
		if (cache.isCached(individual)) {
			return evaluateCachedExecution(GeneticAlgorithm.generation, id, individual);
     	} else {
     		return evaluateNewExecution(GeneticAlgorithm.generation, id, individual);
     	}
    }

	private static Double evaluateNewExecution(long generation, int individualId, FarsiteIndividual individual) {
		Thread currentThread = Thread.currentThread();
		
		String msg = String.format("Individual  started: [ %2s %3s ] %s -> [%s]", generation, individualId, individual, currentThread.getName());
		logger.info(msg);//System.out.println(msg);
		FarsiteExecution execution = executor.run(GeneticAlgorithm.generation, individualId, individual);
		cache.add(execution);
		msg = String.format(       "Individual finished: [ %2s %3s ] %s -> [%s]", generation, individualId, execution, currentThread.getName());
		logger.info(msg);
		System.out.println(String.format(" %3s - %3s ==> [ %2s %3s ] %s -> [%s]", executionIdCount.incrementAndGet(), executionIdPerGenerationCount.incrementAndGet(), generation, individualId, execution, currentThread.getName()));
		return execution.getFireError();
	}

	private static Double evaluateCachedExecution(long generation, int individualId, FarsiteIndividual individual) {
		Thread currentThread = Thread.currentThread();
		
		FarsiteExecution cachedExecution = cache.get(individual);
		Double error = null;
		
		//if (cachedExecution.getMaxSimulatedTime().equals(executor.getSimulatedTime())) {
			String msg = String.format("Individual finished: [ %2s %3s ] %s -> [%s] - CACHED", generation, individualId, cachedExecution, currentThread.getName());
			logger.info(msg);//System.out.println(msg);
			System.out.println(String.format(" %3s - %3s ==> [ %2s %3s ] %s -> [%s] - CACHED", executionIdCount.incrementAndGet(), executionIdPerGenerationCount.incrementAndGet(), generation, individualId, cachedExecution, currentThread.getName()));
			error = cachedExecution.getFireError();
		/*} else {
			String msg = String.format("%2s %3s %s -> [%s] - NaN", generation, individualId, cachedExecution, currentThread.getName());
			logger.info(msg);System.out.println(msg);
			error = Double.NaN;
		}*/
		return error;
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