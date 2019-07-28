package com.edigley.tsp.ui;

public class Main {

	public static void main(String[] args) throws Exception {

		String farsiteExecutor = "/home/edigley/git/spif/fireSimulator86400";
		farsiteExecutor = "target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction";
		String scenarioDir = "/home/edigley/doutorado_uab/git/spif/playpen/cloud/jonquera/";
		
		String cmdPattern = "two-stage-prediction.jar -f %s -s %s";
		String spotsimCMD = String.format(cmdPattern, farsiteExecutor, scenarioDir);
		args = spotsimCMD.split("\\s+");
		CLI.main(args);

	}
	
}
