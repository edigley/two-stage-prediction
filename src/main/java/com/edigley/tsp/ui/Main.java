package com.edigley.tsp.ui;

public class Main {

	public static void main(String[] args) throws Exception {

		String farsiteExecutor = "/home/edigley/git/spif/fireSimulator86400";
		String scenarioDir = "/home/edigley/doutorado_uab/git/spif/playpen/cloud/jonquera/";
		
		String cmdPattern = "two-stage-prediction.jar -f %s -s %s";
		String spotsimCMD = String.format(cmdPattern, farsiteExecutor, scenarioDir);
		args = spotsimCMD.split("\\s+");
		CLI.main(args);

	}
	
}
