package com.edigley.tsp.executors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.comparator.NormalizedSymmetricDifference;
import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.exceptions.TSPFarsiteExecutionException;
import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.ProcessUtil;

import io.jenetics.Genotype;

public class FarsiteExecutor {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutor.class);

	private ScenarioProperties scenarioProperties; 
	
	private File farsiteFile;

	private File scenarioDir;
	
	private Long timeout;
	
	private Long parallelizationLevel = 1L;
	
	private FarsiteIndividualEvaluator evaluator;
	
	public FarsiteExecutor(File farsiteFile, File scenarioDir, Long timeout, Long parallelizationLevel) {
		this.farsiteFile = farsiteFile;
		this.scenarioDir = scenarioDir;
		this.timeout = timeout;
		this.parallelizationLevel = parallelizationLevel;
	}
	
	public FarsiteExecutor(File farsiteFile, File scenarioDir) {
		this.farsiteFile = farsiteFile;
		this.scenarioDir = scenarioDir;
	}
	
	public FarsiteExecutor(String farsiteFilePath, String scenarioDirPath) {
		this(new File(farsiteFilePath), new File(scenarioDirPath));
	}

	public static String toCmdArg(long generation, long id, Genotype<?> gt) {
		String genotypeAsString = FarsiteIndividual.toStringParams(gt);
		return generation + " " + id + " " + genotypeAsString;// + " 1";
	}

	public static String toCmdArg(long generation, long id, FarsiteIndividual individual) {
		return generation + " " + id + " " + individual.toString();// + " 1";
	}
	
	public FarsiteExecution run(long generation, long id, FarsiteIndividual individual) throws RuntimeException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		FarsiteExecution execution = new FarsiteExecution(individual);
		execution.setParallelizationLevel(parallelizationLevel);
		
		Double fireError = Double.NaN;
		
		try {
			fireError = execute(generation, id, individual);
		} catch (TSPFarsiteExecutionException e) {
			logger.error("There was an error when trying to execute individual: " + e.getMessage(), e);
		}
		
		//if (fireError.equals(Double.NaN) || fireError > 9999) {
		//	logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
			File perimeterFile = scenarioProperties.getPerimeterAtT1();
			File predictionFile = scenarioProperties.getShapeFileOutput(generation, id);
			fireError = evaluator.calculateWeightedPredictionError(predictionFile, perimeterFile, scenarioProperties.getTimeToBeSimulated());
		//}
		
		try {
			//Long maxSimulatedTime = evaluator.getSimulatedTime(scenarioProperties.getShapeFileOutput(generation, id));
			Long maxSimulatedTime = evaluator.getSimulatedTime(predictionFile);
			execution.setMaxSimulatedTime(maxSimulatedTime);
		} catch (Exception e) {
			System.err.printf("Couldn't extract maximum simulated time for individual %s - %s\n", individual, e.getMessage());
			logger.warn("Couldn't extract maximum simulated time for inidividual " + individual, e);
		}
		stopWatch.stop();
		
		long executionTime = Math.round(stopWatch.getTime()/1000.0);
		execution.setFireError(fireError);
		execution.setExecutionTime(executionTime);
		execution.setPredictionFile(scenarioProperties.getShapeFileOutput(generation, id));
		
		logger.info(String.format("Finished execution for individual [ %s %s ] %s", generation, id, individual));
		
		return execution;
	}
	
	private Double execute(long generation, long id, FarsiteIndividual individual) throws TSPFarsiteExecutionException {
		String commandPattern = "%s scenario.ini run %s   %s   %s | grep \"adjustmentError\" | head -n1 | awk '{print $9}'";
		String command = String.format(commandPattern, this.farsiteFile.getAbsolutePath(), toCmdArg(generation, id, individual), timeout, parallelizationLevel);
		logger.info(String.format("Going to run farsite wrapper with command < %s > in dir < %s >", command, scenarioDir));
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = command;
		try {
			Process process = Runtime.getRuntime().exec(args, null, scenarioDir);
			
			FarsiteExecutionMonitor.monitorFarsiteExecution(generation, id, individual, scenarioProperties, process);
			
			Double fireError = ProcessUtil.monitorProcessExecution(process, timeout);
			return fireError;
		} catch (Exception e) {
			String message = String.format("Couldn't run farsite for individual [ %2s %3s ] %s -> %s", generation, id, individual, e.getMessage());
			logger.error(message, e);
			throw new TSPFarsiteExecutionException(message, e);
		} finally {
			args[2] = String.format("rm -f " + scenarioProperties.getRasterOutput(generation, id).getAbsolutePath());
			try {
				Runtime.getRuntime().exec(args, null, scenarioDir);
			} catch (IOException e) {
				logger.error(String.format("Couldn't delete output file for individual [ %2s %3s ] %s", generation, id, individual), e);
			}
		}
	}

	public void setScenarioProperties(ScenarioProperties scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}
	
	public void setFitnessEvaluator(FarsiteIndividualEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public Long getTimeout() {
		return timeout;
	}

	public Long getSimulatedTime() {
		return scenarioProperties.getTimeToBeSimulated();
	}

	public static void main(String[] args) throws Exception {
		
		//FarsiteIndividual individual = new FarsiteIndividual("   6   4   4  48  83   10  356  34  67  0.1  ");
		//FarsiteIndividual individual = new FarsiteIndividual("  8   7   7  21  99    5  347  45  35  0.4");
		//File scenarioDir = new File("playpen/fire-scenarios/jonquera/");
		
		FarsiteIndividual individual = new FarsiteIndividual("  9  12  14  22  87   165  353  38  50  1.7");
		
		File farsiteFile = new File("target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction"); 
		File scenarioDir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/");
		
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		
		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir);
		executor.setScenarioProperties(scenarioProperties);
		executor.timeout = 300L;
		executor.parallelizationLevel = 1L;

		long generation = 9;
		long individualId = 9;
		FarsiteExecution execution = executor.run(generation, individualId, individual);
		
		FarsiteExecutionMonitor.release();
		
		File perimeter1File = scenarioProperties.getPerimeterAtT1();
		
		File shapeFile = scenarioProperties.getShapeFileOutput(generation, individualId);
		
		FarsiteIndividualEvaluator evaluator2 = new FarsiteIndividualEvaluator(new NormalizedSymmetricDifference());
		Pair<Long, Double> fireEvolution = evaluator2.getFireEvolution(shapeFile, perimeter1File);
		Long simulatedTime = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();
		
		long timeToBeSimulated = scenarioProperties.getTimeToBeSimulated();
		Double weightedError = evaluator2.calculateWeightedPredictionError(shapeFile, perimeter1File, timeToBeSimulated);
				
		System.out.printf("Header:     %s error weightedError simulatedTime timeToBeSimulated \n", FarsiteExecution.header);
		System.out.printf("Execution: %s %s %s %s %s \n", execution, error, simulatedTime, timeToBeSimulated, weightedError);

	}
	
}
