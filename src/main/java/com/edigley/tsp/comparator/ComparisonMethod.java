package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

public interface ComparisonMethod {

	Double compare(String gAFilePath, String gBFilePath) throws IOException;
	
	Double compare(File gAFile, File gBFile) throws IOException;

	//Double evaluate(File gAFile, File gBFile, long expectedSimulatedTime) throws IOException;

}
