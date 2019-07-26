package com.edigley.tsp.util;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

@SuppressWarnings("deprecation")
public class CLIUtils {
	
	public static CommandLine parseCommandLine(String[] args, Options options, String HELP, String USAGE, String EXECUTION_LINE) {
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
	
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			showMessageAndExit(e);
		}
	
		checkForHelpAsk(options, cmd, HELP, USAGE, EXECUTION_LINE);
	
		return cmd;
	}
	
	public static void showMessageAndExit(Exception e) {
		showMessageAndExit(e.getMessage());
	}

	public static void showMessageAndExit(String message) {
		System.err.println(message);
		System.exit(1);
	}

	public static void checkForHelpAsk(Options options, CommandLine cmd, String HELP, String USAGE, String EXECUTION_LINE) {
		HelpFormatter formatter = new HelpFormatter();
		if (cmd.hasOption(HELP)) {
			formatter.printHelp(EXECUTION_LINE, options);
		} else if (cmd.hasOption(USAGE)) {
			formatter.printHelp(EXECUTION_LINE, options, true);
		} else {
			return;
		}
		System.exit(1);
	}
	
	public static void assertsFilesExists(File...files) {
		for (File file : files) {
			if (!file.exists()) {
				throw new RuntimeException("File doesn't exist: " + file);	
			}
		}
	}
	
}

