package com.edigley.tsp.stages;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.time.StopWatch;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.io.input.ScenarioProperties;

public abstract class Stage {

	protected static transient String msg;
	
	protected CommandLine cmd;
	
	protected File farsiteFile;
	
	protected File scenarioDir;
	
	protected ScenarioProperties scenarioProperties; 
	
	protected List<FarsiteExecution> results;

	// auxiliary flags
	protected boolean prepared = false;
	protected boolean finished = false;
	
	public Stage(CommandLine cmd) {
		this.cmd = cmd;
	}

	public final List<FarsiteExecution> run() throws java.text.ParseException, IOException, ParseException, NoSuchAlgorithmException {
		assert !finished;
		
		if (!prepared) {
			prepare();			
		}
		
		this.results = this.execute();
		
		finished = true;
		
		return this.results;
	}
	
	public abstract void prepare() throws java.text.ParseException, IOException, ParseException, NoSuchAlgorithmException;

	protected abstract List<FarsiteExecution> execute();

	public abstract boolean printSummaryStatistics(StopWatch stopWatch);

	public abstract void releaseResources();

}
