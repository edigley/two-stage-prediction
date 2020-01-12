package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.edigley.tsp.util.shapefile.ShapeFileWriter;

public class GoodnessOfFitTest extends ComparisonMethodTest {

	String shapeFile = null;
	
	@Before
	public void setUp( ) {
		 comparator = new GoodnessOfFit();
	}
	
	void assertComparisonBetweenTheSameShape(String shapeFilePath) throws IOException {
		assertComparison(shapeFilePath, shapeFilePath, 1.0);
	}
	
	void assertComparisonBetweenConcentricCircles(int predictionRadius, int perimeterRadius, double gof) throws IOException {
		String prediction = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + predictionRadius + ".shp";
		String perimeter = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + perimeterRadius + ".shp";
		assertComparison(prediction, perimeter, gof);
	}
	
	@Test
	public void testCalculateGoFForTheSameFile() throws Exception {
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
	public void testCalculateGoGForConcentricCircles() throws Exception {
		
		//assertComparison("jonquera_ignition_buffers/jonquera_ignition_buffer_100.shp", "jonquera_ignition_buffers/jonquera_ignition_buffer_200.shp", 0.25000000000031897);
		
		// prediction equals to the real perimeter
		assertComparisonBetweenConcentricCircles(100, 100, 1.0);
		
		// prediction smaller than the real perimeter
		assertComparisonBetweenConcentricCircles(100, 200, 0.25000000000031897);
		assertComparisonBetweenConcentricCircles(100, 300, 0.11111111111120561);
		assertComparisonBetweenConcentricCircles(100, 400, 0.06249999999997697);
		assertComparisonBetweenConcentricCircles(100, 500, 0.04000000000000573);
		
		// prediction larger than the real perimeter
		assertComparisonBetweenConcentricCircles(200, 100, 0.25000000000031897);
		assertComparisonBetweenConcentricCircles(300, 100, 0.11111111111120561);
		assertComparisonBetweenConcentricCircles(400, 100, 0.06249999999997697);
		assertComparisonBetweenConcentricCircles(500, 100, 0.04000000000000573);

		// prediction ranging from smaller to larger perimeters
		assertComparisonBetweenConcentricCircles(100,  1000,  0.010000000000003588);
		assertComparisonBetweenConcentricCircles(200,  1000,  0.039999999999963315);
		assertComparisonBetweenConcentricCircles(300,  1000,  0.08999999999995574);
		assertComparisonBetweenConcentricCircles(400,  1000,  0.16000000000011635);
		assertComparisonBetweenConcentricCircles(500,  1000,  0.25000000000005385);
		assertComparisonBetweenConcentricCircles(600,  1000,  0.3599999999998458);
		assertComparisonBetweenConcentricCircles(700,  1000,  0.49000000000009075);
		assertComparisonBetweenConcentricCircles(800,  1000,  0.6399999999999768);
		assertComparisonBetweenConcentricCircles(900,  1000,  0.8099999999996329);
		assertComparisonBetweenConcentricCircles(1000, 1000,  1.0);
		assertComparisonBetweenConcentricCircles(1100, 1000,  0.8264462809917268);
		assertComparisonBetweenConcentricCircles(1200, 1000,  0.6944444444443071);
		assertComparisonBetweenConcentricCircles(1300, 1000,  0.591715976331414);
		assertComparisonBetweenConcentricCircles(1400, 1000,  0.5102040816326825);
		assertComparisonBetweenConcentricCircles(1500, 1000,  0.44444444444441006);
		assertComparisonBetweenConcentricCircles(1600, 1000,  0.39062500000006534);
		assertComparisonBetweenConcentricCircles(1700, 1000,  0.3460207612456421);
		assertComparisonBetweenConcentricCircles(1800, 1000,  0.3086419753086454);
		assertComparisonBetweenConcentricCircles(1900, 1000,  0.2770083102493044);
		assertComparisonBetweenConcentricCircles(2000, 1000,  0.2499999999999984);
		assertComparisonBetweenConcentricCircles(3000, 1000,  0.11111111111111861);
		assertComparisonBetweenConcentricCircles(4000, 1000,  0.06250000000000197);
		assertComparisonBetweenConcentricCircles(5000, 1000,  0.04000000000000099);
		
	}

	@Test
	public void testCalculateGoFForRealFirePerimeters() throws Exception {
		assertComparison("shape_1_1.shp", "jonquera_perimeter_1.shp", 0.27865477309465514);
		assertComparison("shape_1_3.shp", "jonquera_perimeter_1.shp", 0.3851732212437519);
	}

	public static void main(String[] args) throws Exception {
		GoodnessOfFit gofEvaluator = new GoodnessOfFit();
		File resourcesDir = new File("src/test/resources/");
		File outputDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output");
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		
		String shapeName = "shape_1_3";
		
		File shapeFile = new File(resourcesDir, shapeName + ".shp");
		File shapeFilePolygon = new File(outputDir, shapeName + "_polygon.shp");
		ShapeFileWriter.saveGeometryPolygon(shapeFile, shapeFilePolygon);

		File map1File = new File(outputDir, "gof_polygon_a.shp");
		File map2File = new File(outputDir, "gof_polygon_b.shp");
		
		File mapAFile = new File(outputDir, "a.shp");
		File mapBFile = new File(outputDir, "b.shp");
		File mapCFile = new File(outputDir, "c.shp");

		System.out.println(gofEvaluator.calculateGoodnessOfFit(shapeFile, perimeter1File));		
		
		/*
		System.out.println(calculateGoodnessOfFit(map1File, map2File));		
		System.out.println(calculateGoodnessOfFit(map2File, map1File));
		System.out.println();
		System.out.println(calculateGoodnessOfFit(map1File, map1File));
		System.out.println(calculateGoodnessOfFit(map2File, map2File));
		System.out.println();
		System.out.println(calculateGoodnessOfFit(mapAFile, mapCFile));
		System.out.println(calculateGoodnessOfFit(mapCFile, mapAFile));
		System.out.println();
		System.out.println(calculateGoodnessOfFit(mapAFile, mapBFile));
		System.out.println(calculateGoodnessOfFit(mapBFile, mapAFile));
		System.out.println();
		System.out.println(calculateGoodnessOfFit(mapBFile, mapCFile));
		System.out.println(calculateGoodnessOfFit(mapCFile, mapBFile));
		 */
	}

}