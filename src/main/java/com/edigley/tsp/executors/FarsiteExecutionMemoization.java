package com.edigley.tsp.executors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class FarsiteExecutionMemoization {

	private static HashMap<FarsiteIndividual, FarsiteExecution> executions = new HashMap<>();
	
	private static FileWriter fw;
	
	public FarsiteExecutionMemoization(File file) {
		try {
			if (!file.exists()) {
				fw = new FileWriter(file);
				fw.append(FarsiteExecution.HEADER + "\n");
				fw.flush();
			} else {
				Scanner sc = new Scanner(file);
				String firstLine = sc.nextLine(); 
				if (!firstLine.equals(FarsiteExecution.HEADER)) {
					String pattern = "First line from file %s doesn't contain expected header. Expected: %s. Actual: %s";
					System.err.println(String.format(pattern, file.getAbsolutePath(), FarsiteExecution.HEADER, firstLine));
					System.exit(5);
				}
				String nextLine;
				while (sc.hasNextLine() && !(nextLine=sc.nextLine()).trim().isEmpty()) {
					FarsiteExecution execution = new FarsiteExecution(nextLine);
					executions.put(execution.getIndividual(), execution);
				}
				sc.close();
				fw = new FileWriter(file, true);
			}
		} catch (IOException e) {
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

	private void save(FarsiteExecution execution) {
		try {
			fw.append(execution.toString() + "\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
