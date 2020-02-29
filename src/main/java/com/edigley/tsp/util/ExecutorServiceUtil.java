package com.edigley.tsp.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorServiceUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceUtil.class);

	public static void release(ExecutorService executorService) {
		String msg = "Going to shutdown the thread pool...";
		logger.debug(msg);System.out.println(msg);
		executorService.shutdownNow();
		try {
			msg = "Going to wait for all the threads in order to finish...";
			logger.debug(msg);System.out.println(msg);
			executorService.awaitTermination(60, TimeUnit.SECONDS);
			msg = "All threads have been finished successfully.";
			logger.debug(msg);System.out.println(msg);
		} catch (InterruptedException e) {
			msg = "Error when trying to shutdown executor service";
			logger.info(msg, e);System.err.println(msg + " " + e.getMessage());
		}
	}
	
}
