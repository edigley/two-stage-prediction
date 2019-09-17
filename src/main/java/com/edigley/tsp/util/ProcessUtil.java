package com.edigley.tsp.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtil {

	private static final Logger logger = LoggerFactory.getLogger(ProcessUtil.class);

	public static Double monitorProcessExecution(Process process, Long timeOut) throws IOException, FileNotFoundException, RuntimeException {

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
					logger.error("Process execution has timed out");
					return Double.NaN;
				}
			} else {
				return processFarsiteExecutionResult(process, errorGobbler, outputGobbler, false);
			}
		} catch (InterruptedException e) {
			logger.error("Process execution was interrupted: ", e);
			System.err.println("Process execution was interrupted: " + e.getMessage());
			throw new RuntimeException("Process execution was interrupted", e);
		}
	}
	
	public static Double monitorProcessExecution(Process process) throws IOException, FileNotFoundException, RuntimeException {
		return monitorProcessExecution(process, null);
	}

	private static Double processFarsiteExecutionResult(Process process, StreamGobbler errorGobbler,
			StreamGobbler outputGobbler, boolean withTimeOut) throws NumberFormatException, InterruptedException {
		String processResult;
		if (processDoesntExitSuccessfully(process, withTimeOut)) {
			logger.debug("process.exitValue(): " + process.exitValue());
			
			if (process.exitValue()==137) {
				logger.debug("Individual killed with exitValue: " + process.exitValue());
				return Double.MAX_VALUE;
			} else {
				processResult = errorGobbler.getContent();
				logger.debug("Farsite execution has failed: " + processResult);
				throw new RuntimeException("Farsite execution has failed: " + process.exitValue());
			}
		} else {
			processResult = outputGobbler.getContent();
			logger.debug("Process Execution has finished successfully with output: " + processResult);
			if (processResult != null && !processResult.trim().isEmpty()) {
				return Double.valueOf(processResult);
			} else {
				throw new RuntimeException("Couldn't parse fireError result: " + processResult);
			}
		}
	}

	public static void killAllDescendants(Process process) throws IOException, InterruptedException {
		
		int processId = extractProcessId(process);
		
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = String.format("kill -9 $(pstree -p %s | grep -o '([0-9]\\+)' | grep -o '[0-9]\\+')", processId);
		Process exec = Runtime.getRuntime().exec(args, null, new File("/tmp"));
		
		StreamGobbler errorGobbler = new StreamGobbler(exec.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(exec.getInputStream());
		if (exec.waitFor()!=0) {
			logger.error("Error when killing all descendant processes for process with id <" + processId + "> " +  errorGobbler.getContent() + " " + outputGobbler.getContent());
		}
	}

	private static int extractProcessId(Process process) {
		try {
			Class<?> cProcessImpl = process.getClass();
			Field fPid = cProcessImpl.getDeclaredField("pid");
			if (!fPid.isAccessible()) {
				fPid.setAccessible(true);
			}
			return fPid.getInt(process);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			logger.error("Error when trying to retrieve process ID ", e);
			System.exit(ErrorCode.RUNTIME_ENVIRONMENT_ACCESS_ERROR);
			return -1;
		}
	}

	private static boolean processDoesntExitSuccessfully(Process process, boolean withTimeOut) throws InterruptedException {
		return withTimeOut ? (process.exitValue() != 0) : (process.waitFor() != 0);
	}

}
