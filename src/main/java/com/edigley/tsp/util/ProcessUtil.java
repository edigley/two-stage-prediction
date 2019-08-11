package com.edigley.tsp.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtil {

	private static final Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

	public static Double monitorProcessExecution(Process process, Long timeOut)
			throws IOException, FileNotFoundException, RuntimeException {

		try {
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			if (timeOut != null) {
				if (process.waitFor(timeOut, TimeUnit.SECONDS)) {
					return processFarsiteExecutionResult(process, errorGobbler, outputGobbler, true);
				} else {
					logger.debug("Farsite execution has timed out");
					System.err.println("Farsite execution has timed out");
					return Double.NaN;
				}
			} else {
				return processFarsiteExecutionResult(process, errorGobbler, outputGobbler, false);
			}
		} catch (InterruptedException e) {
			logger.error("Execução do Process foi interrompida:", e);
			System.err.println("Execução do Process foi interrompida: " + e.getMessage());
			throw new RuntimeException("Farsite execution was interrupted", e);
		}
	}
	
	public static Double monitorProcessExecution(Process process) throws IOException, FileNotFoundException, RuntimeException {
		return monitorProcessExecution(process, null);
	}

	private static Double processFarsiteExecutionResult(Process process, StreamGobbler errorGobbler,
			StreamGobbler outputGobbler, boolean withTimeOut) throws NumberFormatException, InterruptedException {
		String processResult;
		if (waitForProcessFinishing(process, withTimeOut)) {
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
	}

	private static boolean waitForProcessFinishing(Process process, boolean withTimeOut) throws InterruptedException {
		return withTimeOut ? (process.exitValue() != 0) : (process.waitFor() != 0);
	}

}
