package com.edigley.tsp.comparator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

public class ComparisonMethodTest {

	File resourcesDir = new File("src/test/resources/");

	ComparisonMethod comparator;

	void assertComparison(String prediction, String perimeter, double comparisonMetric) throws IOException {
		File perimeter1File = new File(resourcesDir, perimeter);
		File predictionFile = new File(resourcesDir, prediction);

		assertEquals(Double.valueOf(comparisonMetric), comparator.compare(predictionFile, perimeter1File));
	}
	
}
