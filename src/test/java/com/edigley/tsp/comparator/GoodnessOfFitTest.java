package com.edigley.tsp.comparator;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.edigley.tsp.util.shapefile.ShapeFileWriter;

public class GoodnessOfFitTest extends ComparisonMethodTest {

	@Before
	public void setUp( ) {
		 comparator = new GoodnessOfFit();
	}
	
	@Test
	public void testCalculateGoF() throws Exception {
		assertComparison("jonquera_ignition_buffers/jonquera_ignition_buffer_100.shp", "jonquera_ignition_buffers/jonquera_ignition_buffer_200.shp", 0.25000000000031897);
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