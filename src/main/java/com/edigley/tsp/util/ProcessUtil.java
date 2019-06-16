package com.edigley.tsp.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtil {

	private static final Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

	public static Double monitorProcessExecution(Process process) throws IOException, FileNotFoundException, RuntimeException {
		try {
			String processResult;

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			if (process.waitFor() != 0) {
				processResult = errorGobbler.getContent();
				logger.debug("Farsite execution has failed: " + processResult);
				throw new RuntimeException("Farsite execution has failed: " + processResult);
			} else {
				logger.debug("Execution finished.");
				processResult = outputGobbler.getContent();
				logger.debug("Process output: \n" + processResult);
				if (processResult != null && !processResult.trim().isEmpty()) {
					return Double.valueOf(processResult);					
				} else {
					throw new RuntimeException("Couldn't parse fireError result: " + processResult);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Execução do Process foi interrompida:", e);
			System.out.println("Execução do Process foi interrompida: " + e.getMessage());
			throw new RuntimeException("Farsite execution was interrupted", e);
		}
	}

}
