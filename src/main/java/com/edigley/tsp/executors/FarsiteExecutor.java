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

	public FarsiteExecutor(File farsiteFile, File scenarioDir, Long timeout) {
		this.farsiteFile = farsiteFile;
		this.scenarioDir = scenarioDir;
		this.timeout = timeout;
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
		Double fireError = execute(generation, id, individual);
		
		if (fireError.equals(Double.NaN) || fireError > 9999) {
			//System.err.printf("fireError.equals(Double.NaN) or fireError > 9999: " + fireError + "\n");
			logger.error("fireError == Double.NaN  or fireError > 9999: " + fireError);
			File gAFile = scenarioProperties.getPerimeterAtT1();
			File gBFile = scenarioProperties.getOutputFile(generation, id);
			try {
				fireError = Double.parseDouble(String.format("%.6f", ShapeFileUtil.calculatePredictionError(gAFile, gBFile)));
			} catch (Exception e) {
				System.err.printf("Couldn't compare non-finished scenario result for individual %s. Error message: %s\n", individual, e.getMessage());
				logger.error("Couldn't compare non-finished scenario result", e);
			}
		}
		
		try {
			Double maxSimulatedTime = ShapeFileUtil.getSimulatedTime(scenarioProperties.getOutputFile(generation, id));
			execution.setMaxSimulatedTime(maxSimulatedTime);
		} catch (Exception e) {
			System.err.printf("Couldn't extract maximum simulated time for individual %s \n", individual);
			logger.warn("Couldn't extract maximum simulated time for inidividual " + individual, e);
		}
		stopWatch.stop();
		
		long executionTime = Math.round(stopWatch.getTime()/1000.0);
		execution.setFireError(fireError);
		execution.setExecutionTime(executionTime);
		
		return execution;
	}
	
	private Double execute(long generation, long id, FarsiteIndividual individual) throws RuntimeException {
		String pattern = "%s scenario.ini run %s %s | grep \"adjustmentError\" | head -n1 | awk '{print $9}'";
		String command = String.format(pattern, this.farsiteFile.getAbsolutePath(), toCmdArg(generation, id, individual), timeout);
		logger.info("Going to run farsite wrapper with command: " + command);
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = command;
		Process process;
		try {
			process = Runtime.getRuntime().exec(args, null, scenarioDir);
			Double fireError = ProcessUtil.monitorProcessExecution(process, timeout);
			return fireError;
		} catch (Exception e) {
			logger.error("Couldn't run farsite", e);
			throw new RuntimeException(e);
		} finally {
			args[2] = "rm -rf output/raster_" + generation + "_" + id + ".toa";
			try {
				Runtime.getRuntime().exec(args, null, scenarioDir);
			} catch (IOException e) {
				logger.error("Couldn't delete output file", e);
			}
		}
	}

	public void setScenarioProperties(ScenarioProperties scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}

}
