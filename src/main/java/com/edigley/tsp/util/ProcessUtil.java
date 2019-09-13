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
					logger.error("Farsite execution has timed out");
					//System.err.println("Farsite execution has timed out");
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
		if (processDoesntExitSuccessfully(process, withTimeOut)) {
			logger.debug("process.exitValue(): " + process.exitValue());
			
			if (process.exitValue()==137) {
				//try {
					//killAllDescendants(process);
					logger.debug("Individual killed with exitValue: " + process.exitValue());
				//} catch (NoSuchFieldException | SecurityException | IOException | IllegalAccessException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				//}
				return Double.valueOf(9);
			} else {
			
				//System.out.println("process.exitValue(): " + process.exitValue());
				processResult = errorGobbler.getContent();
				logger.debug("Farsite execution has failed: " + processResult);
				throw new RuntimeException("Farsite execution has failed: " + process.exitValue());
				
			}
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

	public static void killAllDescendants(Process process) throws NoSuchFieldException, IllegalAccessException, IOException, InterruptedException {
		
		Class<?> cProcessImpl = process.getClass();
		Field fPid = cProcessImpl.getDeclaredField("pid");
		if (!fPid.isAccessible()) {
			fPid.setAccessible(true);
		}
		System.out.println("fPid.getInt(process): " + fPid.getInt(process));
		//Runtime.getRuntime().exec("kill -9 " + fPid.getInt(process));
		//Runtime.getRuntime().exec("pkill -KILL -P " + fPid.getInt(process));
		//Process exec = Runtime.getRuntime().exec("kill -9 $(pstree -p " + fPid.getInt(process) + " | grep -o '([0-9]\\+)' | grep -o '[0-9]\\+')");
		
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = "kill -9 $(pstree -p " + fPid.getInt(process) + " | grep -o '([0-9]\\+)' | grep -o '[0-9]\\+')";
		//System.out.println("Gonna kill with command: " + args[2]);
		Process exec = Runtime.getRuntime().exec(args, null, new File("/tmp"));
		
		StreamGobbler errorGobbler = new StreamGobbler(exec.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(exec.getInputStream());
		int waitFor = exec.waitFor();
		//System.out.println("Just killed all with result: " + waitFor);

		//System.out.println("errorGobbler: " + errorGobbler.getContent());
		//System.out.println("outputGobbler: " + outputGobbler.getContent());
	}

	private static boolean processDoesntExitSuccessfully(Process process, boolean withTimeOut) throws InterruptedException {
		return withTimeOut ? (process.exitValue() != 0) : (process.waitFor() != 0);
	}

}
