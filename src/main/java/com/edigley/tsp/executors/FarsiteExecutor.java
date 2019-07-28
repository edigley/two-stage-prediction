package com.edigley.tsp.executors;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.util.ProcessUtil;

import io.jenetics.Genotype;

public class FarsiteExecutor {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteExecutor.class);

	private File farsiteFile;

	private File scenarioDir;

	public FarsiteExecutor(File farsiteFile, File scenarioDir) {
		this.farsiteFile = farsiteFile;
		this.scenarioDir = scenarioDir;
	}
	
	public FarsiteExecutor(String farsiteFilePath, String scenarioDirPath) {
		this(new File(farsiteFilePath), new File(scenarioDirPath));
	}

	public static String toFarsiteParams(Genotype<?> gt) {
		return gt.toString().replace("[", "").replace("]", "").replace(",", " ");
	}

	public static String toCmdArg(long generation, long id, Genotype<?> gt) {
		String genotypeAsString = toFarsiteParams(gt);
		return generation + " " + id + " " + genotypeAsString + " 1";
	}

	public Double run(long generation, long id, Genotype<?> gt) throws RuntimeException {
		String pattern = "%s scenario.ini run %s | grep \"adjustmentError\" | head -n1 | awk '{print $9}'";
		String command = String.format(pattern, this.farsiteFile.getAbsolutePath(), toCmdArg(generation, id, gt));
		String[] args = new String[3];
		args[0] = "sh";
		args[1] = "-c";
		args[2] = command;
		Process process;
		try {
			process = Runtime.getRuntime().exec(args, null, scenarioDir);
			Double fireError = ProcessUtil.monitorProcessExecution(process);
			return fireError;
		} catch (IOException e) {
			logger.error("Couldn't run farsite", e);
			throw new RuntimeException(e);
		} finally {
			args[2] = "rm -rf output/raster_" + generation + "_" + id + ".toa";
			try {
				Runtime.getRuntime().exec(args, null, scenarioDir);
			} catch (IOException e) {
				logger.error("Couldn't delete output file", e);
			}
		}
	}

}
