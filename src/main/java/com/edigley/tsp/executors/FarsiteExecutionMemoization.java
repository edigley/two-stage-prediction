package com.edigley.tsp.executors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.entity.FarsiteIndividual;
import com.edigley.tsp.util.ErrorCode;

public class FarsiteExecutionMemoization {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutionMemoization.class);
	
	private static HashMap<FarsiteIndividual, FarsiteExecution> executions = new HashMap<>();
	
	private static FileWriter fw;
	
	private transient String msg;
	
	public FarsiteExecutionMemoization(File file) {
		try {
			if (!file.exists()) {
				fw = new FileWriter(file);
				fw.append(FarsiteExecution.header + "\n");
				fw.flush();
			} else {
				Scanner sc = new Scanner(file);
				String firstLine = sc.nextLine(); 
				if (!firstLine.equals(FarsiteExecution.header.toString())) {
					String pattern = "First line from file %s doesn't contain expected header. Expected: %s. Actual: %s";
					msg = String.format(pattern, file.getAbsolutePath(), FarsiteExecution.header, firstLine);
					logger.error(msg);System.err.println(msg);
					System.exit(ErrorCode.UNEXPECTED_MEMOIZATION_FILE_HEADER);
				}
				String nextLine;
				while (sc.hasNextLine() && !(nextLine=sc.nextLine()).trim().isEmpty()) {
					FarsiteExecution execution = new FarsiteExecution(nextLine);
					msg = String.format("Going to add cached value for individual %s: %s %s", execution.getIndividual(), execution.getFireError(), execution.getExecutionTime()); 
					logger.info(msg);
					executions.put(execution.getIndividual(), execution);
				}
				sc.close();
				fw = new FileWriter(file, true);
			}
		} catch (IOException e) {
			msg = String.format("Error when loading memoization file: %s", e.getMessage());
			logger.error(msg);
			System.err.println(msg);
			e.printStackTrace();
		}
	}
	
	public void add(FarsiteExecution execution) {
		executions.put(execution.getIndividual(), execution);
		save(execution);
	}

	public FarsiteExecution get(FarsiteIndividual individual) {
		return executions.get(individual);
	}
	
	public Double getFireError(FarsiteIndividual individual) {
		FarsiteExecution farsiteExecution = executions.get(individual);
		if (farsiteExecution == null) {
			logger.warn("Attempt to retrieve fire error for non cached individual: " + individual);
		}
		return (farsiteExecution != null) ? farsiteExecution.getFireError() : Double.MAX_VALUE;
	}

	private void save(FarsiteExecution execution) {
		try {
			fw.append(execution.toString() + "\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isCached(FarsiteIndividual individual) {
		return get(individual) != null;
	}
	
	public static void main(String[] args) {
		FarsiteExecutionMemoization cache = new FarsiteExecutionMemoization(new File("playpen/farsite_execution_memoization_7200_seconds.txt"));
		
		FarsiteIndividual individual = new FarsiteIndividual(" 15  13   6  58  99  102  349  36  30  1.0  ");
		// 15  13   6  57  89  102  349  36  30  1,0  0,942831     330      1   7207
		// 15  13   6  57  89  102  349  36  30  1,0  0,942831     330      1   7207
		System.out.println(cache.executions.size());
		
		System.out.println(cache.get(individual));
	}
	
}
