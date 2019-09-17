package com.edigley.tsp.executors;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.input.ScenarioProperties;
import com.edigley.tsp.input.ShapeFileUtil;
import com.edigley.tsp.util.ProcessUtil;

import io.jenetics.Genotype;

public class FarsiteExecutor {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutor.class);

	private ScenarioProperties scenarioProperties; 
	
	private File farsiteFile;

	private File scenarioDir;
	
	private Long timeout;
	
	private Long parallelizationLevel;

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
		Double fireError = execute(generation, id, individual);
		
		if (fireError.equals(Double.NaN) || fireError > 9999) {
			//System.err.printf("fireError.equals(Double.NaN) or fireError > 9999: " + fireError + "\n");
			logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
			File gAFile = scenarioProperties.getPerimeterAtT1();
			File gBFile = scenarioProperties.getShapeFileOutput(generation, id);
			try {
				Double _fireError = ShapeFileUtil.calculatePredictionError(gAFile, gBFile);
				if (fireError.equals(Double.MAX_VALUE)) {
					//fireError = (1 + _fireError);
					fireError = Double.parseDouble(String.format("%.6f", (1 + _fireError)).replace(",", "."));
				} else {
					fireError = Double.parseDouble(String.format("%.6f", _fireError).replace(",", "."));
				}
			} catch (Exception e) {
				System.err.printf("Couldn't compare non-finished scenario result for individual [ %s ]. Error message: %s\n", individual, e.getMessage());
				logger.error("Couldn't compare non-finished scenario result", e);
			}
		}
		
		try {
			Long maxSimulatedTime = ShapeFileUtil.getSimulatedTime(scenarioProperties.getShapeFileOutput(generation, id));
			execution.setMaxSimulatedTime(maxSimulatedTime);
		} catch (Exception e) {
			System.err.printf("Couldn't extract maximum simulated time for individual %s - %s\n", individual, e.getMessage());
			logger.warn("Couldn't extract maximum simulated time for inidividual " + individual, e);
		}
		stopWatch.stop();
		
		long executionTime = Math.round(stopWatch.getTime()/1000.0);
		execution.setFireError(fireError);
		execution.setExecutionTime(executionTime);
		
		return execution;
	}
	
	private Double execute(long generation, long id, FarsiteIndividual individual) throws RuntimeException {
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
			String message = String.format("Couldn't run farsite for individual [%s %s] %s", generation, id, individual);
			logger.error(message, e);
			throw new RuntimeException(message, e);
		} finally {
			args[2] = String.format("rm -f " + scenarioProperties.getRasterOutput(generation, id).getAbsolutePath());
			try {
				Runtime.getRuntime().exec(args, null, scenarioDir);
			} catch (IOException e) {
				logger.error(String.format("Couldn't delete output file for individual [%s %s] %s", generation, id, individual), e);
			}
		}
	}

	public void setScenarioProperties(ScenarioProperties scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}

	public Long getTimeout() {
		return timeout;
	}

	public static void main(String[] args) throws Exception {
		
		//FarsiteIndividual individual = new FarsiteIndividual("   6   4   4  48  83   10  356  34  67  0.1  ");
		FarsiteIndividual individual = new FarsiteIndividual("  9  12  14  22  87   165  353  38  50  1.7");
		//FarsiteIndividual individual = new FarsiteIndividual("  8   7   7  21  99    5  347  45  35  0.4");
		File farsiteFile = new File("target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction"); 
		//File scenarioDir = new File("playpen/fire-scenarios/jonquera/");
		File scenarioDir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/");
		ScenarioProperties scenarioProperties = new ScenarioProperties(scenarioDir);
		FarsiteExecutor executor = new FarsiteExecutor(farsiteFile, scenarioDir);
		executor.setScenarioProperties(scenarioProperties);
		executor.timeout = 300L;
		executor.parallelizationLevel = 1L;
		FarsiteExecution execution = executor.run(9, 9, individual);
		System.out.println("execution: " + execution);
		FarsiteExecutionMonitor.release();
	}

	public Long getSimulatedTime() {
		return scenarioProperties.getSimulatedTime();
	}
	
}
