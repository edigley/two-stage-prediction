package com.edigley.tsp.comparator;

import org.junit.Before;
import org.junit.Test;

public class NormalizedSymmetricDifferenceTest extends ComparisonMethodTest {

	@Before
	public void setUp( ) {
		 comparator = new NormalizedSymmetricDifference();
	}
	
	@Test
	public void testCalculateNSD() throws Exception {
		
		assertComparison("jonquera_ignition_buffers/jonquera_ignition_buffer_100.shp", "jonquera_ignition_buffers/jonquera_ignition_buffer_200.shp", 0.749999999999681);
		assertComparison("shape_1_3.shp", "jonquera_perimeter_1.shp", 0.9148345582325907);
	}

}