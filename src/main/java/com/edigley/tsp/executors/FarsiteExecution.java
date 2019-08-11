package com.edigley.tsp.executors;

public class FarsiteExecution {

	public static String HEADER = FarsiteIndividual.HEADER + " fireError executionTime";
	
	public static int HEADER_LENGTH = HEADER.trim().split("\\s+").length;
	
	private FarsiteIndividual individual;

	private Double fireError;
	
	private long executionTime;
	
	public FarsiteExecution(FarsiteIndividual individual) {
		this.individual = individual;
	}
	
	public FarsiteExecution(String executionAsString) {
		String[] params = executionAsString.trim().split("\\s+");
		assert HEADER_LENGTH <= params.length;
		individual = new FarsiteIndividual(params);
		fireError = Double.valueOf(params[9]);
		executionTime = Long.valueOf(params[10]);
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

	public FarsiteIndividual getIndividual() {
		return individual;
	}

	@Override
	public String toString() {
		//return individual + " " + fireError + " " + executionTime;
		String pattern = "%s %11s %6s";
		return String.format(pattern, individual, fireError, executionTime);
	}
	
}
