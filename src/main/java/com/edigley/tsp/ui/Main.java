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
		
		String tspCMD_Jonquera = "" 
		+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
		+ " -c playpen/fire-scenarios/jonquera/ " 
		+ " -m playpen/executions/jonquera_farsite_execution_memoization_agof.txt "
		+ " -t 1800 " 
		+ " -p 1 " 
		+ " -e agof "
		+ " -s 200 ";// 59873423 98075 47334  9876 720 123 321 159
		
		String tspCMD_Arkadia = "" 
		+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
		+ " -c playpen/fire-scenarios/arkadia/ " 
		+ " -m playpen/executions/arkadia_farsite_execution_memoization_agof.txt "
		+ " -t 1800 " 
		+ " -p 1 "
		+ " -calibrate "
		+ " -e agof "
		+ " -s 201 ";
		
		String tspCMD_Arkadia_Prediction = "" 
		+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
		+ " -c playpen/fire-scenarios/arkadia/ " 
		+ " -m playpen/executions/arkadia_farsite_execution_memoization_agof.txt "
		+ " -t 1800 " 
		+ " -p 1 "
		+ " -predict "
		+ " -b best_calibrated_results_1.txt "
		+ " -e agof "
		+ " -s 201 ";
		
		// to generate a .jpg file referent to the prediction shape file
		String tspCMD_Compare = ""
				+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
				+ " -c playpen/fire-scenarios/jonquera/ "
				+ " -compare  "
				+ " -prediction playpen/execution_agof_seed_21_1/output/shape_1_14.shp "  
				+ " -perimeter  playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp "
				+ " -layer      playpen/fire-scenarios/jonquera/perimetres/jonquera_polygon_from_layer_extent.shp ";

		// to recalculate the fireError for the memoization file
		String tspCMD_Recalculate = ""
				+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
				+ " -c playpen/fire-scenarios/jonquera/ "
				+ " -recalculate  "
				+ " -m playpen/execution_memoization_all_seeds/farsite_execution_memoization_agof_2.txt "
				+ " -perimeter  playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp ";
		
		tspCMD = tspCMD_Recalculate;
		
		// to generate a .jpg file referent to the prediction shape file
		tspCMD_Compare = ""
				+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
				+ " -c playpen/fire-scenarios/arkadia/ "
				+ " -compare  "
				+ " -prediction src/test/resources/scenarios/arkadia/shape_9_517.shp "  
				+ " -perimeter  playpen/fire-scenarios/arkadia/landscape/Per2_utm.shp "
				+ " -layer      playpen/fire-scenarios/arkadia/landscape/arkadia_extent_layer.shp ";
		tspCMD = tspCMD_Compare;
		
		// to generate a .jpg file referent to the prediction shape file
		tspCMD_Compare = ""
				+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
				+ " -c playpen/fire-scenarios/arkadia/ "
				+ " -compare  "
				+ " -prediction /home/edigley/git/two-stage-prediction/playpen/executions/cached/arkadia/execution_nsd_seed_88_1/output/shape_1_2.shp "  
				+ " -perimeter  playpen/fire-scenarios/arkadia/landscape/Per2_utm.shp "
				+ " -layer      playpen/fire-scenarios/arkadia/landscape/arkadia_extent_layer.shp ";
		tspCMD = tspCMD_Compare;

		// to generate a .jpg file referent to the prediction shape file
		tspCMD_Compare = ""
				+ " -f target/nar/two-stage-prediction-0.0.1-SNAPSHOT-amd64-Linux-gcc-executable/bin/amd64-Linux-gcc/two-stage-prediction "  
				+ " -c playpen/fire-scenarios/arkadia/ "
				+ " -compare  "
				+ " -prediction /home/edigley/git/two-stage-prediction/playpen/intermediate_polygons/shape_1_2.shp "  
				+ " -perimeter  /home/edigley/git/two-stage-prediction/playpen/intermediate_polygons/Per2_utm.shp "
				+ " -layer      playpen/fire-scenarios/arkadia/landscape/arkadia_extent_layer.shp ";
		tspCMD = tspCMD_Compare;
		tspCMD = tspCMD_Jonquera;
		tspCMD = tspCMD_Arkadia;
		tspCMD = tspCMD_Arkadia_Prediction;
		args = tspCMD.trim().split("\\s+");
		CLI.main(args);

	}
	
}
