package com.edigley.tsp.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarsiteInputFilesGenerator extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(FarsiteInputFilesGenerator.class);
	
	public void generateWeatherFile(File file) throws IOException {
	}
	
	public void generateWindFile(File file) throws IOException {
		//Month Day Hour Speed Direction CloudCover
		StringBuffer sb = new StringBuffer();
		sb.append("ENGLISH");
		for (int i = 1; i <= 256; i++) {
			sb.append(i).append(" ").append("").append("\n");
		}
		FileWriter adjustmentFile = new FileWriter(file, true);
		adjustmentFile.append(sb);
		adjustmentFile.flush();
		adjustmentFile.close();
	}
	
	public void generateFuelMoistureFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i <= 256; i++) {
			sb.append(i).append(" ").append("1h 10h 100h herb 100").append("\n");
		}
		FileWriter adjustmentFile = new FileWriter(file, true);
		adjustmentFile.append(sb);
		adjustmentFile.flush();
		adjustmentFile.close();
	}
	
	public void generateAdjustmentFile(File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		for (int i = 1; i <= 50; i++) {
			sb.append(i).append(" ").append("1.0").append("\n");
		}
		FileWriter adjustmentFile = new FileWriter(file, true);
		adjustmentFile.append(sb);
		adjustmentFile.flush();
		adjustmentFile.close();
	}
	
}
