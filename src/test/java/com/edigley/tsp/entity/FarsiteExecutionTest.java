package com.edigley.tsp.entity;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.edigley.tsp.comparator.GoodnessOfFit;
import com.edigley.tsp.comparator.NormalizedSymmetricDifference;

public class FarsiteExecutionTest {

	private File resourcesDir = new File("src/test/resources/");
	
	private FarsiteIndividual individual1 = new FarsiteIndividual("  9  12  14  22  87   165  353  38  50  1.7 ");
	private FarsiteIndividual individual2 = new FarsiteIndividual("  6   7  14  37  79    53  350  31  96  1.5 ");
	
	private FarsiteExecution execution1;
	private FarsiteExecution execution2;
	
	@Before
	public void setUp() {
		execution1 = new FarsiteExecution(individual1);
		execution2 = new FarsiteExecution(individual2);
	}
	
	@Test
	public void testExecutionComparison() throws Exception {
		assertComparison("shape_1_1.shp", "shape_1_3.shp");
	}

	private void assertComparison(String shapeFile1Path, String shapeFile2Path) {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile1 = new File(resourcesDir, shapeFile1Path);
		File shapeFile2 = new File(resourcesDir, shapeFile2Path);
		execution1.setPredictionFile(shapeFile1);
		execution2.setPredictionFile(shapeFile2);
	}
	
	@Test
	public void testComparison() {
		
		execution1.setFireError(1.0);
		
		execution2.setFireError(2.0);
		
		assertEquals(execution1.compareTo(execution2), -1);
		assertEquals(execution2.compareTo(execution1), 1);
		assertEquals(execution1.compareTo(execution1), 0);
		assertEquals(execution2.compareTo(execution2), 0);
		
		GoodnessOfFit gofComparator = new GoodnessOfFit();
		execution1.setComparator(gofComparator);
		execution2.setComparator(gofComparator);
		
		assertEquals(execution1.compareTo(execution2), 1);
		assertEquals(execution2.compareTo(execution1), -1);
		assertEquals(execution1.compareTo(execution1), 0);
		assertEquals(execution2.compareTo(execution2), 0);

		
		NormalizedSymmetricDifference nsdComparator = new NormalizedSymmetricDifference();
		execution1.setComparator(nsdComparator);
		execution2.setComparator(nsdComparator);
		
		assertEquals(execution1.compareTo(execution2), -1);
		assertEquals(execution2.compareTo(execution1), 1);
		assertEquals(execution1.compareTo(execution1), 0);
		assertEquals(execution2.compareTo(execution2), 0);
		
	}
	
	@Test
	public void testToString() throws Exception {
		execution1.setParallelizationLevel(1);
		execution1.setExecutionTime(59);
		execution1.setFireError(3.9);
		execution1.setMaxSimulatedTime(480L);
		File shapeFile1 = new File(resourcesDir, "shape_1_1.shp");
		execution1.setPredictionFile(shapeFile1);
		assertEquals("  9  12  14  22  87  165  353  38  50  1,7  3,900000     480      1     59 shape_1_1.shp", execution1.toString());
	}
	
}