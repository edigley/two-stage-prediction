package com.edigley.tsp.comparator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.edigley.tsp.fitness.FarsiteIndividualEvaluator;
import com.edigley.tsp.util.shapefile.ShapeFileWriter;

public class GoodnessOfFitTest {

	private File resourcesDir = new File("src/test/resources/");

	private FarsiteIndividualEvaluator evaluator = new FarsiteIndividualEvaluator(new GoodnessOfFit());

	@Test
	public void testCalculatePredictionGoF() throws Exception {
		assertPredictionGoF("shape_1_1.shp", 180, 0.27865477309465514, 480, 0.104496);
		assertPredictionGoF("shape_1_3.shp", 180, 0.3851732212437519,  480, 0.14444);
	}

	private void assertPredictionGoF(String shapeFilePath, int simulationTime, double gof,
			int totalSimulationTime, double fitnessEvaluation) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, shapeFilePath);

		Pair<Long, Double> fireEvolution = evaluator.getFireEvolution(shapeFile, perimeter1File);

		Long simulatedTime = fireEvolution.getLeft();
		Double predictionFitness = fireEvolution.getRight();

		assertEquals(Long.valueOf(simulationTime), simulatedTime);
		assertEquals(Double.valueOf(gof), predictionFitness);

		Double weightedError = evaluator.calculateWeightedPredictionError(shapeFile, perimeter1File, Long.valueOf(totalSimulationTime));
		assertEquals(Double.valueOf(fitnessEvaluation), weightedError);
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