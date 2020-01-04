package com.edigley.tsp.fitness;

import java.io.File;
import java.io.IOException;

public interface IndividualEvaluator {

	Double evaluate(String gAFilePath, String gBFilePath) throws IOException;
	
	Double evaluate(File gAFile, File gBFile) throws IOException;

}
