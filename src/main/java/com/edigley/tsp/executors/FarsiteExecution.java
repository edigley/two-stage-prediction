package com.edigley.tsp.executors;

import com.edigley.tsp.input.FileHeader;

public class FarsiteExecution {

	public static final FileHeader header = new FileHeader(FarsiteIndividual.header + " fireError maxSimulatedTime executionTime ");
	
	public static final int PARAMS_START_POS = FarsiteIndividual.header.length;
	
	private FarsiteIndividual individual;

	private Double fireError;
	
	private Double maxSimulatedTime;
	
	private long executionTime;
	
	public FarsiteExecution(FarsiteIndividual individual) {
		this.individual = individual;
	}
	
	public FarsiteExecution(String executionAsString) {
		String[] params = executionAsString.trim().split("\\s+");
		assert header.length <= params.length;
		individual = new FarsiteIndividual(params);
		fireError = Double.valueOf(params[PARAMS_START_POS]);
		executionTime = Long.valueOf(params[PARAMS_START_POS+1]);
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

	@Override
	public String toString() {
		String pattern = "%s %11s %.0f %6s";
		return String.format(pattern, individual, fireError, maxSimulatedTime, executionTime);
	}
	
}
