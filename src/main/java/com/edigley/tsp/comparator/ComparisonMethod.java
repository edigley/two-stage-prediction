package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import com.edigley.tsp.entity.FarsiteExecution;

public interface ComparisonMethod extends Comparator<FarsiteExecution>{

	Double compare(String gAFilePath, String gBFilePath) throws IOException;
	
	Double compare(File gAFile, File gBFile) throws IOException;

	Double defineAdjustmentFactor(Long effectivelySimulatedTime, Long expectedSimulatedTime);

	int compare(FarsiteExecution e1, FarsiteExecution e2);
	
}
