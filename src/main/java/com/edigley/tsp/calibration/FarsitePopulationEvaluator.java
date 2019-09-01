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
	
	private static AtomicInteger generationCount = new AtomicInteger(0);
	private int generation;
	
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
		System.out.println("---> Runtime.getRuntime().availableProcessors(): " + Runtime.getRuntime().availableProcessors());
		cache = theCache;
	}
	
	public static void setFarsiteExecutor(FarsiteExecutor theExecutor) {
		executor = theExecutor;
	}
	
	@Override
	public ISeq<Phenotype<IntegerGene, Double>> eval(Seq<Phenotype<IntegerGene, Double>> population) {
		
		generation = generationCount.getAndIncrement();
		
		populationSummary("Start ", generation, population);
		
		Long nonEvaluatedPhen = population.stream().filter(p->!p.isEvaluated()).count();
		CountDownLatch latch = new CountDownLatch(nonEvaluatedPhen.intValue());
		
		population.stream()
			.filter(p -> !p.isEvaluated())
			.forEach(
				p -> {
					final String individual = FarsiteIndividual.toStringParams(p.getGenotype());
					msg = " --> " + individual + " " + p.isEvaluated();
					logger.debug(msg);
					executorService.submit(() -> {p.eval(FarsitePopulationEvaluator::eval); latch.countDown();});
				}
			);	
		
		try {
			msg = "Going to wait for all the threads in order to finish...";
			logger.debug(msg);System.out.println(msg);
			latch.await();
			msg = "All threads have been finished successfully.";
			logger.debug(msg);System.out.println(msg);
		} catch (InterruptedException e) {
			msg = "Error while expecting all tasks to finish";
			logger.error(msg, e);System.err.println(msg + " " + e.getMessage());
		}
		
		ISeq<Phenotype<IntegerGene, Double>> evaluatedPopulation = population.map(p->p.eval(pt -> cache.get(new FarsiteIndividual(pt)).getFireError())).asISeq();
		populationSummary("Finish", generation, evaluatedPopulation);
		return evaluatedPopulation;
	}

	private void populationSummary(String position, int generation, Seq<Phenotype<IntegerGene, Double>> population) {
		
		long nPhenEvaluated = population.stream().filter(Phenotype::isEvaluated).count();
		Long maxPhenGeneration = population.stream().mapToLong(Phenotype::getGeneration).max().orElse(0L);
		int popSize = population.size();
		
		String pattern = "Evaluation.%s -->  %s : %s / %s / %s / %s / %s <---------";
		String header = "#Evaluation Calls / GA.generation / maxPhenGeneration / popSize / nPhenEvaluated";
		
		msg = String.format(pattern, position, header, generation, GeneticAlgorithm.generation, maxPhenGeneration, popSize, nPhenEvaluated);
		
		logger.debug(msg);System.out.println(msg);
	}

	private static Double eval(Genotype<IntegerGene> gt) {
		
		Thread currentThread = Thread.currentThread();
		
     	int id = GeneticAlgorithm.atomicCount.incrementAndGet();
     	FarsiteIndividual individual = new FarsiteIndividual(gt);
     	logger.debug(String.format("Going to check cached value for individual %s", individual));
     	FarsiteExecution cachedExecution = cache.get(individual);
     	logger.debug(String.format("Cached value for individual %s: %s", individual, cachedExecution));
		if (cachedExecution != null) {
			msg = String.format("%2s %3s %s -> [%s] - CACHED", GeneticAlgorithm.generation, id, cachedExecution, currentThread.getName());
			logger.info(msg);System.out.println(msg);
     		return cachedExecution.getFireError();
     	} else {
			FarsiteExecution execution = executor.run(GeneticAlgorithm.generation, id, individual);
			cache.add(execution);
			msg = String.format("%2s %3s %s -> [%s]", GeneticAlgorithm.generation, id, execution, currentThread.getName());
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

}