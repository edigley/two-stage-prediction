package com.edigley.tsp.io.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.util.ErrorCode;

public class ScenarioProperties {

	private static final Logger logger = LoggerFactory.getLogger(ScenarioProperties.class);
	
	//defaultValues for properties
	public static final String SCENARIO_FILE_NAME = "scenario.ini";
	public static final int NUMBER_OF_GENERATIONS = 10;
	public static final int POPULATION_SIZE = 25;
	public static final int NUMBER_OF_BEST_INDIVIDUALS = 10;
	public static final double RECOMBINATION_PROBABILITY = 0.4;
	public static final double MUTATION_PROBABILITY = 0.1;

	//CoordinateReferenceSystem
	public static final String CRS = "EPSG:4326";

	public static Long DEFAULT_EXPECTED_SIMULATED_TIME;// = 480L;
	
	private Properties scenarioProperties;

	private File scenarioFile;
	
	private File landscapeFile;
	
	private File perimeterAtT0File;
	private File perimeterAtT1File;
	
	private File inputDir;
	private File outputDir;
	
	private int numGenerations;
	private int populationSize;
	
	private Long farsiteParallelizationLevel;
	
	private long farsiteExecutionTimeout;
	
	private double crossoverProbability;
	
	private double mutationProbability;
	
	private Integer farsiteStartMonth;
	private Integer farsiteStartDay;
	private Integer farsiteStartHour;
	private Integer farsiteStartMin;
	private LocalDateTime startTime;
	
	private Integer farsiteEndMonth;
	private Integer farsiteEndHour;
	private Integer farsiteEndDay;
	private Integer farsiteEndMin;
	private LocalDateTime endTime;
	
	private long timeToBeSimulated;

	private Double maxTolerableFireError;

	private Long maxNonProgressingIterations;

	private int numberOfBestIndividuals;

	private File landscapeLayerExtentFile;

	public ScenarioProperties(File scenarioDir) throws FileNotFoundException, IOException {
		assert scenarioDir.exists();
		scenarioFile = new File(scenarioDir, SCENARIO_FILE_NAME);
		assert scenarioFile.exists() : "Scenario file is mandatory: " + scenarioFile.getAbsolutePath();
		
		if (!scenarioFile.exists()) {
			System.err.println("Scenario file doesn't exist: " + scenarioFile.getAbsolutePath());
			System.exit(ErrorCode.NONEXISTENT_SCENARIO_FILE);
		}
		
		scenarioProperties = new Properties();
		scenarioProperties.load(new FileInputStream(scenarioFile));
		
		this.landscapeFile = new File(scenarioDir, scenarioProperties.getProperty("landscapeFile"));
		this.landscapeLayerExtentFile = new File(scenarioDir, scenarioProperties.getProperty("landscapeLayerExtentFile"));
		
		assert landscapeFile.exists() : "Landscape file is mandatory: " + landscapeFile.getAbsolutePath();
		
		if (!landscapeFile.exists()) {
			System.err.println("Landscape file doesn't exist: " + landscapeFile.getAbsolutePath());
			System.exit(ErrorCode.NONEXISTENT_LANDSCAPE_FILE);
		}
		
		this.perimeterAtT0File = new File(scenarioDir, scenarioProperties.getProperty("ignitionFile"));
		this.perimeterAtT1File = new File(scenarioDir, scenarioProperties.getProperty("real_fire_map_t1").trim().replace(".asc", ".shp"));
		
		this.inputDir = new File(scenarioDir, scenarioProperties.getProperty("input_path").trim());
		this.outputDir = new File(scenarioDir, scenarioProperties.getProperty("output_path").trim());
		
		if (!this.inputDir.exists()) {
			logger.info("Going to create inputDir: " + this.inputDir.getAbsolutePath());
			boolean wasCreated = this.inputDir.mkdir();
			if (!wasCreated) {
				System.err.println("Input directory didn't exist and couldn't be create: " + inputDir.getAbsolutePath());
				System.exit(ErrorCode.NONEXISTENT_INPUT_DIR);
			}
		}
		
		if (!this.outputDir.exists()) {
			logger.info("Going to create outputDir: " + this.outputDir.getAbsolutePath());
			boolean wasCreated = this.outputDir.mkdir();
			if (!wasCreated) {
				System.err.println("Output directory didn't exist and couldn't be create: " + outputDir.getAbsolutePath());
				System.exit(ErrorCode.NONEXISTENT_OUTPUT_DIR);
			}
		}
		
		this.numGenerations = Integer.valueOf(scenarioProperties.getProperty("numGenerations", Integer.toString(NUMBER_OF_GENERATIONS).trim()));
		this.populationSize = Integer.valueOf(scenarioProperties.getProperty("population_size", Integer.toString(POPULATION_SIZE).trim()));
		this.crossoverProbability = Double.valueOf(scenarioProperties.getProperty("pCrossover", Double.toString(RECOMBINATION_PROBABILITY).trim()));
		this.mutationProbability = Double.valueOf(scenarioProperties.getProperty("pMutation", Double.toString(MUTATION_PROBABILITY).trim()));
		
		this.numberOfBestIndividuals = Integer.valueOf(scenarioProperties.getProperty("numberOfBestIndividuals", Integer.toString(NUMBER_OF_BEST_INDIVIDUALS)).trim());
		
		this.maxTolerableFireError = Double.valueOf(scenarioProperties.getProperty("maxTolerableFireError", "1.5").trim());
		this.maxNonProgressingIterations = Long.valueOf(scenarioProperties.getProperty("maxNonProgressingIterations", "10").trim());
		
		this.farsiteParallelizationLevel = Long.valueOf(scenarioProperties.getProperty("num_threads", "1").trim());
		this.farsiteExecutionTimeout = Integer.valueOf(scenarioProperties.getProperty("ExecutionLimit").trim());
		
		this.farsiteStartMonth = Integer.valueOf(scenarioProperties.getProperty("StartMonth").trim());
		this.farsiteStartDay = Integer.valueOf(scenarioProperties.getProperty("StartDay").trim());
		assert scenarioProperties.getProperty("StartHour").trim().length() == 4 : "StartHour should be specified including minutes: P. ex: 1200";
		this.farsiteStartHour = Integer.valueOf(scenarioProperties.getProperty("StartHour").trim())/100;
		this.farsiteStartMin = Integer.valueOf(scenarioProperties.getProperty("StartMin").trim());
		
		this.startTime = LocalDateTime.of(2011, farsiteStartMonth, farsiteStartDay, farsiteStartHour, farsiteStartMin);
		
		this.farsiteEndMonth = Integer.valueOf(scenarioProperties.getProperty("EndMonth").trim());
		this.farsiteEndDay = Integer.valueOf(scenarioProperties.getProperty("EndDay").trim());
		assert scenarioProperties.getProperty("EndHour").trim().length() == 4 : "EndHour should be specified including minutes: P. ex: 1800";
		this.farsiteEndHour = Integer.valueOf(scenarioProperties.getProperty("EndHour").trim())/100;
		this.farsiteEndMin = Integer.valueOf(scenarioProperties.getProperty("EndMin").trim());
		
		this.endTime = LocalDateTime.of(2011, farsiteEndMonth, farsiteEndDay, farsiteEndHour, farsiteEndMin);
		
		this.timeToBeSimulated = Duration.between(startTime, endTime).getSeconds()/60;
		DEFAULT_EXPECTED_SIMULATED_TIME = this.timeToBeSimulated; 
		
		logger.info("ScenarioProperties.startTime                    : " + this.startTime);
		logger.info("ScenarioProperties.endTime                      : " + this.endTime);
		logger.info("ScenarioProperties.simulatedTime                : " + this.timeToBeSimulated + " minutes");
		
		logger.info("ScenarioProperties.numGenerations               : " + this.numGenerations);
		logger.info("ScenarioProperties.populationSize               : " + this.populationSize);
		logger.info("ScenarioProperties.crossoverProbability         : " + this.crossoverProbability);
		logger.info("ScenarioProperties.mutationProbability          : " + this.mutationProbability);
		
		logger.info("ScenarioProperties.maxTolerableFireError        : " + this.maxTolerableFireError);
		logger.info("ScenarioProperties.maxNonProgressingIterations  : " + this.maxNonProgressingIterations);
		
	}
	
	public File getPerimeterAtT1() {
		return perimeterAtT1File;
	}

	public File getRasterOutput(long generation, long individual) {
		assert generation >= 0;
		assert individual >= 1;
		return new File(outputDir, String.format("raster_%s_%s.toa", generation, individual));
	}
	
	public File getShapeFileOutput(long generation, long individual) {
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

	public long getTimeToBeSimulated() {
		return timeToBeSimulated;
	}

	public Double getMaxTolerableFireError() {
		return maxTolerableFireError;
	}

	public Long getMaxNonProgressingIterations() {
		return maxNonProgressingIterations;
	}

	public int getNumberOfBestIndividuals() {
		return numberOfBestIndividuals;
	}

	public File getLandscapeLayerExtentFile() {
		return landscapeLayerExtentFile;
	}

}
