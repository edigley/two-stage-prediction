package com.edigley.tsp.calibration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.executors.FarsiteIndividual;
import com.edigley.tsp.input.ScenarioProperties;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.MultiPointCrossover;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.Engine.Evaluator;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

class FarsitePopulationEvaluator implements Evaluator<IntegerGene, Double> {

	@Override
	public ISeq<Phenotype<IntegerGene, Double>> evaluate(Seq<Phenotype<IntegerGene, Double>> population) {
		long nPhenEvaluated = population.stream().filter(Phenotype::isEvaluated).count();
		String pattern = "FarsitePopulationEvaluator.evaluate ---------> %s : %s / %s / %s / %s <---------";
		long phen1Gen = population.get(0).getGeneration();
		int popSize = population.size();
		String header = "Start - Generation / phen1Gen / popSize / nPhenEvaluated";
		System.out.println(String.format(pattern, header, GeneticAlgorithm.generation, phen1Gen, popSize, nPhenEvaluated));
		population.stream().forEach(
			p -> System.out.println(" --> " + FarsiteIndividual.toStringParams(p.getGenotype()) + " " + p.isEvaluated())
		);
		return population.asISeq();
	}
	
}

public class GeneticAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);
	
	private static FarsiteExecutionMemoization cache;
	
	private static final int NUMBER_OF_GENERATIONS = 10;
	private static final int POPULATION_SIZE = 25;
	private static final double RECOMBINATION_PROBABILITY = 0.4;
	private static final double MUTATION_PROBABILITY = 0.1;
	
	static long generation = 0;
	private static long id = 0;
	
	private static FarsiteExecutor executor;
	
	private static ScenarioProperties scenarioProperties;
	
    public void setExecutor(FarsiteExecutor executor) {
		GeneticAlgorithm.executor = executor;
	}

    public void setFarsiteExecutionCache(FarsiteExecutionMemoization cache) {
    	GeneticAlgorithm.cache = cache;
    }
    
	private static synchronized Double eval(Genotype<IntegerGene> gt) {
     	id++;
     	String logMessage = "";
     	FarsiteIndividual individual = new FarsiteIndividual(gt);
     	FarsiteExecution cachedExecution = cache.get(individual);
		if (cachedExecution != null) {
			logMessage = String.format("%2s %3s %s -> CACHED", generation, id, cachedExecution);
			System.out.println(logMessage);
			logger.info(logMessage);
     		return cachedExecution.getFireError();
     	} else {
			FarsiteExecution execution = executor.run(generation, id, individual);
			cache.add(execution);
			logMessage = String.format("%2s %3s %s", generation, id, execution);
			System.out.println(logMessage);
			logger.info(logMessage);
	    	return execution.getFireError();
     	}
    }

    private final Genotype<IntegerGene> genotypeFactory = Genotype.of(
    	IntegerChromosome.of(  2,  15, 1 ),   // fms - t1
    	IntegerChromosome.of(  2,  15, 1 ),   // fms - t10
    	IntegerChromosome.of(  2,  15, 1 ),   // fms - t100
    	IntegerChromosome.of( 20,  70, 1 ),   // fms - t1000
    	IntegerChromosome.of( 70, 100, 1 ),   // fms - t10000
    	IntegerChromosome.of(  0, 150, 1 ),   //  ws - wind speed
    	IntegerChromosome.of(345, 360, 1 ),   //  wd - wind direction (  0, 360, 1 )
    	IntegerChromosome.of( 30,  50, 1 ),   //  th - temperature highest
    	IntegerChromosome.of( 30, 100, 1 ),   //  hh - humidity highest
    	IntegerChromosome.of( -9, 9, 1 )      //  adj - adjustment factor
    );

	private final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

	private Engine<IntegerGene, Double> engine = Engine
    	.builder(GeneticAlgorithm::eval, genotypeFactory)
    	.populationSize(POPULATION_SIZE)
    	.minimizing()
    	.alterers(new MultiPointCrossover<>(RECOMBINATION_PROBABILITY), new Mutator<>(MUTATION_PROBABILITY))
    	//.evaluator(new FarsitePopulationEvaluator())
    	.mapping(EvolutionResult.toUniquePopulation())
    	.build();

	public Phenotype<IntegerGene,Double> run() {
        Phenotype<IntegerGene, Double> result = engine.stream()
        	.limit(NUMBER_OF_GENERATIONS)
        	.peek(r -> {
        		generation = r.getGeneration();
        		System.out.println(String.format("Generation / Durations : %s / %s", generation, r.getDurations().getEvaluationDuration()));
        	})
        	.peek(statistics)
        	.peek(r -> System.out.println(statistics))
        	.collect(EvolutionResult.toBestPhenotype());

        return result;
	}

	public void setScenarioProperties(ScenarioProperties scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}
    
}