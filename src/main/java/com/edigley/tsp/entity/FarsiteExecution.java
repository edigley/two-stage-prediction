package com.edigley.tsp.entity;

import java.io.File;

import com.edigley.tsp.comparator.ComparisonMethod;
import com.edigley.tsp.io.FileHeader;

public class FarsiteExecution implements Comparable<FarsiteExecution> {

	public static final FileHeader header = new FileHeader(FarsiteIndividual.header + " fireError maxSimulatedTime parallelizationLevel executionTime predictionFile ");
	
	public static final int PARAMS_START_POS = FarsiteIndividual.header.length;
	
	private FarsiteIndividual individual;
	
	private ComparisonMethod comparator;

	private Double fireError;
	
	private Long maxSimulatedTime;
	
	private long parallelizationLevel;

	private long executionTime;
	
	private File predictionFile;
	
	public FarsiteExecution(FarsiteIndividual individual) {
		this.individual = individual;
	}
	
	public FarsiteExecution(String executionAsString) {
		String[] params = executionAsString.trim().split("\\s+");
		assert header.length <= params.length;
		individual = new FarsiteIndividual(params);
		int currentPosition = 0;
		fireError = Double.valueOf(params[PARAMS_START_POS + currentPosition]);
		currentPosition++;
		maxSimulatedTime = Long.valueOf(params[PARAMS_START_POS + currentPosition]);
		currentPosition++;
		parallelizationLevel = Long.valueOf(params[PARAMS_START_POS + currentPosition]);
		currentPosition++;
		executionTime = Long.valueOf(params[PARAMS_START_POS + currentPosition]);
		currentPosition++;
		predictionFile = new File(params[PARAMS_START_POS + currentPosition]);
	}

	public Double getFireError() {
		return fireError;
	}
	
	public void setFireError(Double fireError) {
		this.fireError = fireError;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public void setMaxSimulatedTime(Long maxSimulatedTime) {
		this.maxSimulatedTime = maxSimulatedTime;
	}
	
	public FarsiteIndividual getIndividual() {
		return individual;
	}
	
	public long getParallelizationLevel() {
		return parallelizationLevel;
	}
	
	public void setParallelizationLevel(long parallelizationLevel) {
		this.parallelizationLevel = parallelizationLevel;
	}

	@Override
	public String toString() {
		String pattern = "%s  %.6f  %6s %6s %6s %s";
		String predictionFilePath = (predictionFile != null) ? predictionFile.getPath() : null;
		return String.format(pattern, individual, fireError, maxSimulatedTime, parallelizationLevel, executionTime, predictionFilePath);
	}

	public Long getMaxSimulatedTime() {
		return maxSimulatedTime;
	}

	@Override
	public int compareTo(FarsiteExecution o) {
		if (this.comparator != null) {
			return this.comparator.compare(this, o);
		}
		return this.fireError.compareTo(o.fireError);
	}

	public File getPredictionFile() {
		return predictionFile;
	}

	public void setPredictionFile(File predictionFile) {
		this.predictionFile = predictionFile;
	}

	public void setComparator(ComparisonMethod comparator) {
		this.comparator = comparator;
	}

	public static void main(String[] args) {
		File predictionFile = new File("playpen/execution_agof_seed_21_1/output/shape_9_1041.shp");
		System.out.println(predictionFile.getName());
		System.out.println(predictionFile.getPath());
		System.out.println(predictionFile.getAbsolutePath());
	}
	
}
