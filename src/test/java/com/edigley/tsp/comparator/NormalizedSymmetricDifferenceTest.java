package com.edigley.tsp.comparator;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class NormalizedSymmetricDifferenceTest extends ComparisonMethodTest {

	@Before
	public void setUp( ) {
		 comparator = new NormalizedSymmetricDifference();
	}
	
	void assertComparisonBetweenTheSameShape(String shapeFilePath) throws IOException {
		assertComparison(shapeFilePath, shapeFilePath, 0.0);
	}
	
	void assertComparisonBetweenConcentricCircles(int predictionRadius, int perimeterRadius, double nsd) throws IOException {
		String prediction = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + predictionRadius + ".shp";
		String perimeter = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + perimeterRadius + ".shp";
		assertComparison(prediction, perimeter, nsd);
	}
	
	@Test
	public void testCalculateNSDForTheSameFile() throws Exception {
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_100.shp"); 
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_200.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_500.shp"); 
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_1000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_2000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_3000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_4000.shp");
		assertComparisonBetweenTheSameShape("shape_1_1.shp");
		assertComparisonBetweenTheSameShape("shape_1_3.shp");
	}
	
	@Test
	public void testCalculateNSDForConcentricCircles() throws Exception {
		
		// prediction equals to the real perimeter
		assertComparisonBetweenConcentricCircles(100, 100, 0.0);
		
		// prediction smaller than the real perimeter
		assertComparisonBetweenConcentricCircles(100, 200, 0.749999999999681);
		assertComparisonBetweenConcentricCircles(100, 300, 0.8888888888887944);
		assertComparisonBetweenConcentricCircles(100, 400, 0.937500000000023);
		assertComparisonBetweenConcentricCircles(100, 500, 0.9599999999999943);
		
		// prediction larger than the real perimeter
		assertComparisonBetweenConcentricCircles(200, 100,  2.999999999994897);
		assertComparisonBetweenConcentricCircles(300, 100,  7.999999999992345);
		assertComparisonBetweenConcentricCircles(400, 100, 15.000000000005896);
		assertComparisonBetweenConcentricCircles(500, 100, 23.999999999996422);

	}

	@Test
	public void testCalculateNSDForRealFirePerimeters() throws Exception {
		assertComparison("shape_1_3.shp", "jonquera_perimeter_1.shp", 0.9148345582325907);
	}
	
}