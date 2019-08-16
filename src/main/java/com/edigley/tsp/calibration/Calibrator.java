package com.edigley.tsp.calibration;

import static com.edigley.tsp.ui.CLI.FARSITE;
import static com.edigley.tsp.ui.CLI.SCENARIO;
import static com.edigley.tsp.ui.CLI.MEMOIZATION;
import static com.edigley.tsp.ui.CLI.TIME_OUT;
import static com.edigley.tsp.util.CLIUtils.assertsFilesExists;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.executors.FarsiteExecutionMemoization;
import com.edigley.tsp.executors.FarsiteExecutor;
import com.edigley.tsp.executors.FarsiteIndividual;
import com.edigley.tsp.input.ScenarioProperties;

import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;

public class Calibrator {

	private static final Logger logger = LoggerFactory.getLogger(Calibrator.class);
	
	private CommandLine cmd;
	
	private File farsiteFile;
	
	private File scenarioDir;
	
	private ScenarioProperties scenarioProperties; 
	
	private GeneticAlgorithm geneticAlgorithm;
	
	private Phenotype<IntegerGene, Double> result;

	// auxiliary flags
	private boolean prepared = false;
	private boolean finished = false;
	
	public Calibrator(CommandLine cmd) {
		this.cmd = cmd;
	}

	public void prepare() throws java.text.ParseException, IOException, ParseException {
		farsiteFile = (File) cmd.getParsedOptionValue(FARSITE);
		scenarioDir = (File) cmd.getParsedOptionValue(SCENARIO);
		
		assertsFilesExists(farsiteFile, scenarioDir, new File(scenarioDir, ScenarioProperties.SCENARIO_FILE_NAME));
		
		geneticAlgorithm = new GeneticAlgorithm();
		
		scenarioProperties = new ScenarioProperties(scenarioDir);
		geneticAlgorithm.setScenarioProperties(scenarioProperties);
		
		Long farsiteExecutionTimeOut = (Long) cmd.getParsedOptionValue(TIME_OUT);
		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir, farsiteExecutionTimeOut);
		executor.setScenarioProperties(scenarioProperties);
		geneticAlgorithm.setExecutor(executor);
		
		FarsiteExecutionMemoization cache;
		if (cmd.hasOption(MEMOIZATION)) {
			cache = new FarsiteExecutionMemoization(((File) cmd.getParsedOptionValue(MEMOIZATION)));
		} else {
			cache = new FarsiteExecutionMemoization(new File("farsite_execution_memoization_default.txt"));
		}
		
		geneticAlgorithm.setFarsiteExecutionCache(cache);
		
		prepared = true;
		
	}
	
	public void run() throws java.text.ParseException, IOException, ParseException {
		assert !finished;
		
		if (!prepared) {
			prepare();			
		}
		
		this.result = geneticAlgorithm.run();
		
		finished = true;
	}

	public void printSummaryStatistics(StopWatch stopWatch) {
        logger.info("Genetic - Best Result:\n" + result);
        System.out.println("Genetic - Best Result:\n" + FarsiteIndividual.toStringParams(result.getGenotype()) + " " + result.getFitness());
	}

	public void releaseResources() {
	}

}
