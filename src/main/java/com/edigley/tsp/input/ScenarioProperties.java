package com.edigley.tsp.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ScenarioProperties {

	//defaultValues for properties
	public static final String SCENARIO_FILE_NAME = "scenario.ini";
	public static final int NUMBER_OF_GENERATIONS = 10;
	public static final int POPULATION_SIZE = 25;
	public static final double RECOMBINATION_PROBABILITY = 0.4;
	public static final double MUTATION_PROBABILITY = 0.1;
	
	private Properties scenarioProperties;

	private File scenarioFile;
	
	private File landscapeFile;
	
	private File perimeterAtT0File;
	
	private File perimeterAtT1File;
	
	private File outputDir;
	
	private int numGenerations;
	
	private int populationSize;
	
	private Long farsiteParallelizationLevel;
	
	private long farsiteExecutionTimeout;
	
	private double crossoverProbability;
	
	private double mutationProbability;

	public ScenarioProperties(File scenarioDir) throws FileNotFoundException, IOException {
		assert scenarioDir.exists();
		scenarioFile = new File(scenarioDir, SCENARIO_FILE_NAME);
		assert scenarioFile.exists();
		scenarioProperties = new Properties();
		scenarioProperties.load(new FileInputStream(scenarioFile));
		
		this.landscapeFile = new File(scenarioDir, scenarioProperties.getProperty("landscapeFile"));
		this.perimeterAtT0File = new File(scenarioDir, scenarioProperties.getProperty("ignitionFile"));
		this.perimeterAtT1File = new File(scenarioDir, scenarioProperties.getProperty("real_fire_map_t1").trim().replace(".asc", ".shp"));
		this.outputDir = new File(scenarioDir, scenarioProperties.getProperty("output_path").trim());
		this.numGenerations = Integer.valueOf(scenarioProperties.getProperty("numGenerations", Integer.toString(NUMBER_OF_GENERATIONS).trim()));
		this.populationSize = Integer.valueOf(scenarioProperties.getProperty("population_size", Integer.toString(POPULATION_SIZE).trim()));
		this.crossoverProbability = Double.valueOf(scenarioProperties.getProperty("pCrossover", Double.toString(RECOMBINATION_PROBABILITY).trim()));
		this.mutationProbability = Double.valueOf(scenarioProperties.getProperty("pMutation", Double.toString(MUTATION_PROBABILITY).trim()));
		this.farsiteParallelizationLevel = Long.valueOf(scenarioProperties.getProperty("num_threads", "1").trim());
		this.farsiteExecutionTimeout = Integer.valueOf(scenarioProperties.getProperty("ExecutionLimit").trim());
		
	}
	
	public File getPerimeterAtT1() {
		return perimeterAtT1File;
	}
	
	public File getOutputFile(long generation, long individual) {
		assert generation >= 0;
		assert individual >= 1;
		return new File(outputDir, String.format("shape_%s_%s.shp", generation, individual));
	}

	public static void main(String[] args) throws Exception {

		String scenarioDir = "/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/";

		File scenarioFile = new File(scenarioDir, "scenario.ini");

		System.out.println(scenarioFile.exists());
		System.out.println(scenarioFile.getCanonicalPath());

		FileInputStream ip = new FileInputStream(scenarioFile);
		Properties prop = new Properties();
		prop.load(ip);

		System.out.println(prop);

		File p1File = new File(scenarioDir, prop.getProperty("real_fire_map_t1").replace(".asc", ".shp"));
		File outputDir = new File(scenarioDir, prop.getProperty("output_path"));
		File outputFile = new File(outputDir, String.format("shape_%s_%s.shp", 0, 6));

		System.out.println(p1File.exists());
		System.out.println(outputFile.exists());

		System.out.println(p1File);
		System.out.println(outputFile);
	}

	public int getNumGenerations() {
		return numGenerations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public Long getFarsiteParallelizationLevel() {
		return farsiteParallelizationLevel;
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public double getMutationProbability() {
		return mutationProbability;
	}

	public long getFarsiteExecutionTimeout() {
		return farsiteExecutionTimeout;
	}

	public File getLandscapeFile() {
		return landscapeFile;
	}

	public File getPerimeterAtT0File() {
		return perimeterAtT0File;
	}

}
