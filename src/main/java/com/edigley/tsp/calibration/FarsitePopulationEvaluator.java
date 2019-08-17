package com.edigley.tsp.calibration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteIndividual;

import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Engine.Evaluator;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;

public class FarsitePopulationEvaluator implements Evaluator<IntegerGene, Double> {

	private static final Logger logger = LoggerFactory.getLogger(FarsitePopulationEvaluator.class);
	private static transient String msg;
	
	@Override
	public ISeq<Phenotype<IntegerGene, Double>> evaluate(Seq<Phenotype<IntegerGene, Double>> population) {
		long nPhenEvaluated = population.stream().filter(Phenotype::isEvaluated).count();
		String pattern = "FarsitePopulationEvaluator.evaluate ---------> %s : %s / %s / %s / %s <---------";
		long phen1Gen = population.get(0).getGeneration();
		int popSize = population.size();
		String header = "Start - Generation / phen1Gen / popSize / nPhenEvaluated";
		msg = String.format(pattern, header, GeneticAlgorithm.generation, phen1Gen, popSize, nPhenEvaluated);
		logger.debug(msg);
		System.out.println(msg);
		population.stream().forEach(
			p -> {
				msg = " --> " + FarsiteIndividual.toStringParams(p.getGenotype()) + " " + p.isEvaluated();
				logger.debug(msg);
				System.out.println(msg);
			}
		);
		return population.asISeq();
	}

}