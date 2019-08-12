package com.edigley.tsp.ui;

import static com.edigley.tsp.util.CLIUtils.parseCommandLine;

import java.io.File;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.calibration.Calibrator;

import io.jenetics.prngine.LCG64ShiftRandom;
import io.jenetics.util.RandomRegistry;

public class CLI {

	static {
		RandomRegistry.setRandom(new LCG64ShiftRandom.ThreadSafe(123));
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CLI.class);
	
	public static final String FARSITE = "f";
	
	public static final String SCENARIO = "s";
	
	public static final String MEMOIZATION = "m";

	public static final String TIME_OUT = "t";

	public static final String HELP = "help";

	public static final String USAGE = "usage";
	
	public static final String EXECUTION_LINE = "java -jar two-stage-prediction.jar";
	
	public static void main(String[] args) throws Exception {

		Locale.setDefault(new Locale("en", "US"));
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		CommandLine cmd = parseCommandLine(args, prepareOptions(), HELP, USAGE, EXECUTION_LINE);
		
		Calibrator calibrator = new Calibrator(cmd);
		calibrator.prepare();
		calibrator.run();

		stopWatch.stop();
		
		calibrator.printSummaryStatistics(stopWatch);
		
		calibrator.releaseResources();

	}

	public static Options prepareOptions() {
		Options options = new Options();
		Option executable = new Option(FARSITE, "farsite", true, "Farsite executable file path");
		Option scenario = new Option(SCENARIO, "scenario", true, "Scenario directory path");
		Option memoization = new Option(MEMOIZATION, "memoization", true, "Memoization file path");
		Option timeOut = new Option(TIME_OUT, "timeout", true, "Timeout for each individual execution");
		
		executable.setRequired(true);
		scenario.setRequired(true);
		memoization.setRequired(false);
		timeOut.setRequired(false);
		
		executable.setType(File.class);
		scenario.setType(File.class);
		memoization.setType(File.class);
		timeOut.setType(Number.class);
		
		options.addOption(executable);
		options.addOption(scenario);
		options.addOption(memoization);
		options.addOption(timeOut);
		return options;
	}

}

