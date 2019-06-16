package com.edigley.tsp;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteExecutor;

import io.jenetics.Genotype;
import io.jenetics.IntegerChromosome;
import io.jenetics.IntegerGene;
import io.jenetics.MultiPointCrossover;
import io.jenetics.Mutator;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import io.jenetics.engine.Engine.Evaluator;

class DirectExecutor implements Executor {
	public void execute(Runnable r) {
		r.run();
	}
};

class TSPEvaluator implements Evaluator<IntegerGene, Double> {

	static int generation = 0;
	
	@Override
	public ISeq<Phenotype<IntegerGene, Double>> evaluate(Seq<Phenotype<IntegerGene, Double>> population) {
		long nPhenEvaluated = population.stream().filter(Phenotype::isEvaluated).count();
		String format = "---------> %s : %s / %s / %s / %s <---------";
		long phen1Gen = population.get(0).getGeneration();
		int popSize = population.size();
		String header = "Start - Generation / phen1Gen / popSize / nPhenEvaluated";
		System.out.println(String.format(format, header, Genetic.generation, phen1Gen, popSize, nPhenEvaluated));
		population.stream().forEach(p -> System.out.println(" --> " + FarsiteExecutor.toFarsiteParams(p.getGenotype()) + " " + p.isEvaluated()));
		return population.asISeq();
	}
	
}

public class Genetic {
	
	static long generation = 0;
	private static long id = 0;
	
	private static final Logger logger = LoggerFactory.getLogger(Genetic.class);
	
    private static synchronized Double eval(Genotype<IntegerGene> gt) {
     	id++;
		Double fireError = FarsiteExecutor.run(generation, id, gt);
		System.out.println(FarsiteExecutor.toCmdArg(generation, id, gt) + " " + fireError);
		logger.info(FarsiteExecutor.toCmdArg(generation, id, gt) + " " + fireError);
    	return fireError;
    }

    public static void main(String[] args) {
    	
    	final EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();
    	
        final Genotype<IntegerGene> genotypeFactory = Genotype.of(
        	IntegerChromosome.of(2, 15, 1),   // fms - t1
        	IntegerChromosome.of(2, 15, 1),   // fms - t10
        	IntegerChromosome.of(2, 15, 1),   // fms - t100
        	IntegerChromosome.of(20, 70, 1),  // fms - t1000
        	IntegerChromosome.of(70, 100, 1), // fms - t10000
        	IntegerChromosome.of(0, 150, 1),  //  ws - wind speed
        	IntegerChromosome.of(0, 360, 1),  //  wd - wind direction
        	IntegerChromosome.of(30, 50, 1),  //  th - temperature highest
        	IntegerChromosome.of(30, 100, 1)  //  hh - humidity highest
        );
        
        Engine<IntegerGene, Double> engine = Engine
        	.builder(Genetic::eval, genotypeFactory)
        	.populationSize(25)
        	.minimizing()
        	.alterers(new MultiPointCrossover<>(0.4), new Mutator<>(0.1))
        	.evaluator(new TSPEvaluator())
        	.mapping(EvolutionResult.toUniquePopulation())
        	.build();

        Phenotype<IntegerGene, Double> result = engine
        	.stream()
        	.limit(10)
        	.peek(r -> {
        		generation = r.getGeneration();
        		System.out.println(String.format("Generation / Durations : %s / %s", generation, r.getDurations().getEvaluationDuration()));
        	})
        	.peek(statistics)
        	.peek(r -> System.out.println(statistics))
        	.collect(EvolutionResult.toBestPhenotype());

        logger.info("Genetic - Best Result:\n" + result);
        System.out.println("Genetic - Best Result:\n" + FarsiteExecutor.toFarsiteParams(result.getGenotype()) + " " + result.getFitness());
        
    }

}