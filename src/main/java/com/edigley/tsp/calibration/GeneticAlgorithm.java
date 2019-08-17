package com.edigley.tsp.calibration;

import java.io.File;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteExecution;
import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.executors.FarsiteIndividual;
import com.edigley.tsp.input.ScenarioProperties;

import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.MultiPointCrossover;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.util.RandomRegistry;

public class GeneticAlgorithm {
	
	private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithm.class);
	private static transient String msg;
	
	private static FarsiteExecutionMemoization cache;
	
	private int NUMBER_OF_GENERATIONS;
	private int POPULATION_SIZE;
	private double RECOMBINATION_PROBABILITY;
	private double MUTATION_PROBABILITY;
	
	static long generation = 0;
	private static long id = 0;
	
	private static FarsiteExecutor executor;
	
	private ScenarioProperties scenarioProperties;

    private Genotype<IntegerGene> genotypeFactory;

    private Engine<IntegerGene, Double> engine;

    private final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();
	
    int count = 0;
    private GeneticAlgorithm() {
    	
    	RandomRegistry.setRandom(new LCG64ShiftRandom.ThreadSafe(123));
    	
		Stream<Genotype<IntegerGene>> instances = genotypeFactory.instances();
		
		//System.out.println("instances.count(): " + instances.count());
		//System.out.println("instances.findFirst(): " + new FarsiteIndividual(instances.findFirst().get()));
		instances.forEach(i -> {
			if (++count < 25) {
				System.out.println("instance: " + new FarsiteIndividual(i));
			} else {
				System.exit(5);
			}
			
		});
		
		Iterator<Chromosome<IntegerGene>> it = genotypeFactory.iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		System.out.println("genotypeFactory.length(): " + genotypeFactory.length());
		System.out.println("genotypeFactory.getChromosome(0): " + genotypeFactory.getChromosome(0));
		System.out.println("genotypeFactory.getChromosome(): " + genotypeFactory.getChromosome());
		Iterator<Chromosome<IntegerGene>> it2 = genotypeFactory.iterator();
		while(it2.hasNext()) {
			System.out.println(it2.next());
		}
		
		//System.out.println("this.engine.getPopulationSize(): " + this.engine.getPopulationSize());
		
		System.exit(45);

    }

	public GeneticAlgorithm(ScenarioProperties scenarioProperties) {
		this(scenarioProperties, null);
	}
    
	public GeneticAlgorithm(ScenarioProperties scenarioProperties, Long seed) {
		
		if (seed != null) {
			RandomRegistry.setRandom(new LCG64ShiftRandom.ThreadSafe(seed));
		}
		
		this.scenarioProperties = scenarioProperties;
		
		this.NUMBER_OF_GENERATIONS = this.scenarioProperties.getNumGenerations();
		this.POPULATION_SIZE = this.scenarioProperties.getPopulationSize();
		this.MUTATION_PROBABILITY = this.scenarioProperties.getMutationProbability();
		this.RECOMBINATION_PROBABILITY = this.scenarioProperties.getCrossoverProbability();
		
		this.genotypeFactory = createGenotypeFactory();
		
		this.engine = prepareEvolutionEngine();

	}
	
	private static synchronized Double eval(Genotype<IntegerGene> gt) {
     	id++;
     	FarsiteIndividual individual = new FarsiteIndividual(gt);
     	logger.debug(String.format("Going to check cached value for individual %s", individual));
     	FarsiteExecution cachedExecution = cache.get(individual);
     	logger.debug(String.format("Cached value for individual %s: %s", individual, cachedExecution));
		if (cachedExecution != null) {
			msg = String.format("%2s %3s %s -> CACHED", generation, id, cachedExecution);
			logger.info(msg);
			System.out.println(msg);
     		return cachedExecution.getFireError();
     	} else {
			FarsiteExecution execution = executor.run(generation, id, individual);
			cache.add(execution);
			msg = String.format("%2s %3s %s", generation, id, execution);
			logger.info(msg);
			System.out.println(msg);
	    	return execution.getFireError();
     	}
    }

	private Genotype<IntegerGene> createGenotypeFactory() {
		return Genotype.of(
        	IntegerChromosome.of(  2,  15, 1 ),   // fms - t1
        	IntegerChromosome.of(  2,  15, 1 ),   // fms - t10
        	IntegerChromosome.of(  2,  15, 1 ),   // fms - t100
        	IntegerChromosome.of( 20,  70, 1 ),   // fms - t1000
        	IntegerChromosome.of( 70, 100, 1 ),   // fms - t10000
        	IntegerChromosome.of(  0, 150, 1 ),   //  ws - wind speed
        	IntegerChromosome.of(345, 360, 1 ),   //  wd - wind direction (  0, 360, 1 )
        	IntegerChromosome.of( 30,  50, 1 ),   //  th - temperature highest
        	IntegerChromosome.of( 30, 100, 1 ),   //  hh - humidity highest
        	IntegerChromosome.of( -9, 9, 1 )      // adj - adjustment factor
	    );
	}
	
	private Engine<IntegerGene, Double> prepareEvolutionEngine() {
		return Engine.builder(GeneticAlgorithm::eval, genotypeFactory)
	    	.populationSize(POPULATION_SIZE)
	    	.minimizing()
	    	.alterers(
	    		new MultiPointCrossover<>(RECOMBINATION_PROBABILITY), 
	    		new Mutator<>(MUTATION_PROBABILITY)
	    	)
	    	//.evaluator(new FarsitePopulationEvaluator())
	    	.mapping(EvolutionResult.toUniquePopulation())
	    	.build();
	}
	
	public FarsiteExecution run() {
        Phenotype<IntegerGene, Double> bestPhenotype = engine.stream()
        	.limit(NUMBER_OF_GENERATIONS)
        	.peek(r -> {
        		generation = r.getGeneration();
        		long duration = r.getDurations().getEvaluationDuration().get(ChronoUnit.SECONDS);
        		String pattern = "Generation = %s / Durations = %s";
				System.out.println(String.format(pattern, generation, duration));
        	})
        	.peek(statistics)
        	.peek(r -> System.out.println(statistics))
        	.collect(EvolutionResult.toBestPhenotype());

        FarsiteIndividual bestIndividual = new FarsiteIndividual(bestPhenotype.getGenotype());
        
        return cache.get(bestIndividual);
	}

    public void setExecutor(FarsiteExecutor executor) {
		GeneticAlgorithm.executor = executor;
	}

    public void setFarsiteExecutionCache(FarsiteExecutionMemoization cache) {
    	GeneticAlgorithm.cache = cache;
    }

    public static void main(String[] args) throws Exception {
    	File scenarioDir = new File("playpen/fire-scenarios/jonquera/");
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
    	GeneticAlgorithm ga = new GeneticAlgorithm();
	}
    
}