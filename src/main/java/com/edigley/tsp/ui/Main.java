package com.edigley.tsp.ui;

public class Main {

	public static void main(String[] args) throws Exception {

		String farsiteExecutor = "/home/edigley/git/spif/fireSimulator86400";
		farsiteExecutor = "target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction";
		String scenarioDir = "/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/";
		scenarioDir = "/home/edigley/doutorado_uab/git/spif/playpen/cloud/jonquera/";
		String memoizationFile = "/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/farsite_memoization_file.txt";
		memoizationFile = "/home/edigley/doutorado_uab/git/spif/playpen/cloud/jonquera/farsite_memoization_file_test.txt";
		long timeout = 5;
		
		String cmdPattern = "two-stage-prediction.jar -f %s -s %s -m %s -t %s";
		String tspCMD = String.format(cmdPattern, farsiteExecutor, scenarioDir, memoizationFile, timeout);
		
		tspCMD = ""
		+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction " 
		+ " -c playpen/fire-scenarios/jonquera/ "
		+ " -m playpen/farsite_execution_memoization_60_seconds_with_adj_factor_and_max_simulated_time.txt " 
		+ " -t 60"
		+ " -p 4"
		+ " -s 123";
		
		args = tspCMD.trim().split("\\s+");
		CLI.main(args);

	}
	
}
