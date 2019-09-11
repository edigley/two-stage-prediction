package com.edigley.tsp.calibration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.engine.Limits;
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
	
	static long generation = 1;

	public static AtomicInteger atomicCount = new AtomicInteger(0);
	
	private static FarsiteExecutor executor;
	
	private ScenarioProperties scenarioProperties;

    private Genotype<IntegerGene> genotypeFactory;

    private Engine<IntegerGene, Double> engine;

    private final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();
	
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
        	IntegerChromosome.of( -9,   9, 1 )    // adj - adjustment factor
	    );
	}
	
	private Engine<IntegerGene, Double> prepareEvolutionEngine() {
		return new Engine
			.Builder<IntegerGene, Double>(FarsitePopulationEvaluator.getInstance(), genotypeFactory)
		    .executor(FarsitePopulationEvaluator.executorService)
	    	.populationSize(POPULATION_SIZE)
	    	.minimizing()
	    	.alterers(
	    		new MultiPointCrossover<>(RECOMBINATION_PROBABILITY), 
	    		new Mutator<>(MUTATION_PROBABILITY)
	    	)
	    	/*.mapping(EvolutionResult.toUniquePopulation())*/
	    	.build();
	}
	
	public FarsiteExecution run() {
        Phenotype<IntegerGene, Double> bestPhenotype = null; 
        List<EvolutionResult<IntegerGene, Double>> collect = engine.stream(/*population*/)
        	//.limit(Limits.byFitnessThreshold(0.3))
        	//.limit(Limits.byExecutionTime(Duration.ofDays(10)))
        	.limit(NUMBER_OF_GENERATIONS)
        	.peek(r -> {
        		generation = r.getGeneration();
        		long duration = r.getDurations().getEvaluationDuration().get(ChronoUnit.SECONDS);
        		String pattern = "Generation = %s / Evaluation Duration = %s\n";
				System.out.println(String.format(pattern, generation, duration));
        	})
        	/*.peek(statistics)
        	.peek(r -> System.out.println(statistics))
        	*/.collect(Collectors.toList());
        	//.collect(EvolutionResult.toBestPhenotype());
        
        System.out.println("---> collect.size(): " + collect.size());
        EvolutionResult<IntegerGene, Double> evolutionResult = collect.get(collect.size()-1);
        
        //evolutionResult.
        
        System.out.println("Going to perform the last evaluation...");
        
        FarsitePopulationEvaluator.getInstance().eval(evolutionResult.getPopulation());
        
        System.out.println("Last evaluation have been performed.");
        
        FarsiteIndividual bestIndividual = new FarsiteIndividual(evolutionResult.getBestPhenotype().getGenotype());
        
        return cache.get(bestIndividual);
	}

    public void setExecutor(FarsiteExecutor executor) {
		GeneticAlgorithm.executor = executor;
	}

    public void setFarsiteExecutionCache(FarsiteExecutionMemoization cache) {
    	GeneticAlgorithm.cache = cache;
    	FarsitePopulationEvaluator.setCache(cache);
    	FarsitePopulationEvaluator.setFarsiteExecutor(executor);
    }
   
}