package com.edigley.tsp.executors;

import com.edigley.tsp.input.FileHeader;

public class FarsiteExecution {

	public static final FileHeader header = new FileHeader(FarsiteIndividual.header + " fireError maxSimulatedTime parallelizationLevel executionTime ");
	
	public static final int PARAMS_START_POS = FarsiteIndividual.header.length;
	
	private FarsiteIndividual individual;

	private Double fireError;
	
	private Double maxSimulatedTime;
	
	private long parallelizationLevel;

	private long executionTime;
	
	public FarsiteExecution(FarsiteIndividual individual) {
		this.individual = individual;
	}
	
	public FarsiteExecution(String executionAsString) {
		String[] params = executionAsString.trim().split("\\s+");
		assert header.length <= params.length;
		individual = new FarsiteIndividual(params);
		fireError = Double.valueOf(params[PARAMS_START_POS]);
		maxSimulatedTime = Double.valueOf(params[PARAMS_START_POS+1]);
		parallelizationLevel = Long.valueOf(params[PARAMS_START_POS+2]);
		executionTime = Long.valueOf(params[PARAMS_START_POS+3]);
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

	public void setMaxSimulatedTime(Double maxSimulatedTime) {
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
		String pattern = "%s  %.6f  %6.0f %6s %6s";
		return String.format(pattern, individual, fireError, maxSimulatedTime, parallelizationLevel, executionTime);
	}
	
}
