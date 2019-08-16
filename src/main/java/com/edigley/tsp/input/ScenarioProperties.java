package com.edigley.tsp.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ScenarioProperties {

	public static final String SCENARIO_FILE_NAME = "scenario.ini";
	
	private File scenarioFile;
	
	private File perimeterAtT1File;
	
	private File outputDir;
	
	private Properties scenarioProperties;

	public ScenarioProperties(File scenarioDir) throws FileNotFoundException, IOException {
		assert scenarioDir.exists();
		this.scenarioFile = new File(scenarioDir, SCENARIO_FILE_NAME);
		assert scenarioFile.exists();
		this.scenarioProperties = new Properties();
		scenarioProperties.load(new FileInputStream(scenarioFile));
		
		this.perimeterAtT1File = new File(scenarioDir, scenarioProperties.getProperty("real_fire_map_t1").replace(".asc", ".shp"));
		this.outputDir = new File(scenarioDir, scenarioProperties.getProperty("output_path"));
	}
	
	public File getPerimeterAtT1() {
		return perimeterAtT1File;
	}
	
	public File getOutputFile(long generation, long individual) {
		assert generation >= 0;
		assert individual >= 1;
		return new File(outputDir, String.format("shape_%s_%s.shp", generation, individual));
	}

	public static void main(String[] args) throws Exception {

		String scenarioDir = "/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/";

		File scenarioFile = new File(scenarioDir, "scenario.ini");

		System.out.println(scenarioFile.exists());
		System.out.println(scenarioFile.getCanonicalPath());

		FileInputStream ip = new FileInputStream(scenarioFile);
		Properties prop = new Properties();
		prop.load(ip);

		System.out.println(prop);

		File p1File = new File(scenarioDir, prop.getProperty("real_fire_map_t1").replace(".asc", ".shp"));
		File outputDir = new File(scenarioDir, prop.getProperty("output_path"));
		File outputFile = new File(outputDir, String.format("shape_%s_%s.shp", 0, 6));

		System.out.println(p1File.exists());
		System.out.println(outputFile.exists());

		System.out.println(p1File);
		System.out.println(outputFile);
	}

}
