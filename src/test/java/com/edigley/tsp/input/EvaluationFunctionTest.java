package com.edigley.tsp.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.edigley.tsp.io.output.FarsiteOutputProcessor;

public class EvaluationFunctionTest {

	private File resourcesDir = new File("src/test/resources/");
	
	@Test
	public void testCalculatePredictionError() throws Exception {
		assertPredictionError("shape_1_1.shp", 180, 1.0251414755268, 480, 2.733711);
		assertPredictionError("shape_1_2.shp", 180, 1.7444638604033933, 480, 4.651904);
		assertPredictionError("shape_1_3.shp", 180, 0.9148345582325907, 480, 2.439559);
		assertPredictionError("shape_1_4.shp", 210, 1.042416539547481, 480, 2.382666);
		assertPredictionError("shape_1_5.shp", 210, 1.5451125291507293, 480, 3.531686);
		assertPredictionError("shape_1_6.shp", 180, 0.815167290542097, 480, 2.173779);
		assertPredictionError("shape_1_7.shp", 210, 0.8160090969975168, 480, 1.865164);
		assertPredictionError("shape_1_8.shp", 300, 1.6657076974539835, 480, 2.665132);
		assertPredictionError("shape_1_9.shp", 120, 0.8471167980612184, 480, 3.388467);
		assertPredictionError("shape_1_10.shp", 210, 0.8217408634472545, 480, 1.878265);
		assertPredictionError("shape_1_11.shp", 180, 0.9505158036715838, 480, 2.534709);
		assertPredictionError("shape_1_12.shp", 210, 0.8251794236260561, 480, 1.886124);
		assertPredictionError("shape_1_13.shp", 480, 1.0448400621280833, 480, 1.04484);
		assertPredictionError("shape_1_14.shp", 150, 1.1970710672061255, 480, 3.830627);
		assertPredictionError("shape_1_15.shp", 300, 0.9521614891673417, 480, 1.523458);
		assertPredictionError("shape_1_16.shp", 180, 0.8929591191980059, 480, 2.381224);
		assertPredictionError("shape_1_17.shp", 210, 0.9264217012330717, 480, 2.117535);
		assertPredictionError("shape_1_18.shp", 150, 0.9563834015776541, 480, 3.060427);
		assertPredictionError("shape_1_19.shp", 210, 1.1186722908345175, 480, 2.556965);
		assertPredictionError("shape_1_20.shp", 270, 1.7684421343254315, 480, 3.143897);
		assertPredictionError("shape_1_21.shp", 210, 0.9161998431883785, 480, 2.094171);
		assertPredictionError("shape_1_22.shp", 150, 0.8897961624557404, 480, 2.847348);
		assertPredictionError("shape_1_23.shp", 150, 0.8074799519557437, 480, 2.583936);
		assertPredictionError("shape_1_24.shp", 210, 0.7987478725291002, 480, 1.825709);
		assertPredictionError("shape_1_25.shp", 150, 0.9115578177308359, 480, 2.916985);
		assertPredictionError("shape_1_26.shp", 150, 0.8313948417414055, 480, 2.660463);
		assertPredictionError("shape_1_27.shp", 210, 0.8403973631545142, 480, 1.920908);
		assertPredictionError("shape_1_28.shp", 210, 0.9670213338968012, 480, 2.210334);
		assertPredictionError("shape_1_29.shp", 240, 1.7057582788815953, 480, 3.411517);
		assertPredictionError("shape_1_30.shp", 120, 1.9829366011599692, 480, 7.931746);
	}

	@Test
	public void testCalculateNormalizedSymmetricDifference() throws Exception {
		assertPredictionError("shape_1_1.shp", 1.0251414755268);
		assertPredictionError("shape_1_2.shp", 1.7444638604033933);
		assertPredictionError("shape_1_3.shp", 0.9148345582325907);
		assertPredictionError("shape_1_4.shp", 1.042416539547481);
		assertPredictionError("shape_1_5.shp", 1.5451125291507293);
		assertPredictionError("shape_1_6.shp", 0.815167290542097);
		assertPredictionError("shape_1_7.shp", 0.8160090969975168);
		assertPredictionError("shape_1_8.shp", 1.6657076974539835);
		assertPredictionError("shape_1_9.shp", 0.8471167980612184);
		assertPredictionError("shape_1_10.shp", 0.8217408634472545);
		assertPredictionError("shape_1_11.shp", 0.9505158036715838);
		assertPredictionError("shape_1_12.shp", 0.8251794236260561);
		assertPredictionError("shape_1_13.shp", 1.0448400621280833);
		assertPredictionError("shape_1_14.shp", 1.1970710672061255);
		assertPredictionError("shape_1_15.shp", 0.9521614891673417);
		assertPredictionError("shape_1_16.shp", 0.8929591191980059);
		assertPredictionError("shape_1_17.shp", 0.9264217012330717);
		assertPredictionError("shape_1_18.shp", 0.9563834015776541);
		assertPredictionError("shape_1_19.shp", 1.1186722908345175);
		assertPredictionError("shape_1_20.shp", 1.7684421343254315);
		assertPredictionError("shape_1_21.shp", 0.9161998431883785);
		assertPredictionError("shape_1_22.shp", 0.8897961624557404);
		assertPredictionError("shape_1_23.shp", 0.8074799519557437);
		assertPredictionError("shape_1_24.shp", 0.7987478725291002);
		assertPredictionError("shape_1_25.shp", 0.9115578177308359);
		assertPredictionError("shape_1_26.shp", 0.8313948417414055);
		assertPredictionError("shape_1_27.shp", 0.8403973631545142);
		assertPredictionError("shape_1_28.shp", 0.9670213338968012);
		assertPredictionError("shape_1_29.shp", 1.7057582788815953);
		assertPredictionError("shape_1_30.shp", 1.9829366011599692);
	}
	
	@Test
	public void testCalculateFireEvolution() throws Exception {
		assertFireEvolution("shape_1_1.shp", 180, 1.0251414755268);
		assertFireEvolution("shape_1_2.shp", 180, 1.7444638604033933);
		assertFireEvolution("shape_1_3.shp", 180, 0.9148345582325907);
		assertFireEvolution("shape_1_4.shp", 210, 1.042416539547481);
		assertFireEvolution("shape_1_5.shp", 210, 1.5451125291507293);
		assertFireEvolution("shape_1_6.shp", 180, 0.815167290542097);
		assertFireEvolution("shape_1_7.shp", 210, 0.8160090969975168);
		assertFireEvolution("shape_1_8.shp", 300, 1.6657076974539835);
		assertFireEvolution("shape_1_9.shp", 120, 0.8471167980612184);
		assertFireEvolution("shape_1_10.shp", 210, 0.8217408634472545);
		assertFireEvolution("shape_1_11.shp", 180, 0.9505158036715838);
		assertFireEvolution("shape_1_12.shp", 210, 0.8251794236260561);
		assertFireEvolution("shape_1_13.shp", 480, 1.0448400621280833);
		assertFireEvolution("shape_1_14.shp", 150, 1.1970710672061255);
		assertFireEvolution("shape_1_15.shp", 300, 0.9521614891673417);
		assertFireEvolution("shape_1_16.shp", 180, 0.8929591191980059);
		assertFireEvolution("shape_1_17.shp", 210, 0.9264217012330717);
		assertFireEvolution("shape_1_18.shp", 150, 0.9563834015776541);
		assertFireEvolution("shape_1_19.shp", 210, 1.1186722908345175);
		assertFireEvolution("shape_1_20.shp", 270, 1.7684421343254315);
		assertFireEvolution("shape_1_21.shp", 210, 0.9161998431883785);
		assertFireEvolution("shape_1_22.shp", 150, 0.8897961624557404);
		assertFireEvolution("shape_1_23.shp", 150, 0.8074799519557437);
		assertFireEvolution("shape_1_24.shp", 210, 0.7987478725291002);
		assertFireEvolution("shape_1_25.shp", 150, 0.9115578177308359);
		assertFireEvolution("shape_1_26.shp", 150, 0.8313948417414055);
		assertFireEvolution("shape_1_27.shp", 210, 0.8403973631545142);
		assertFireEvolution("shape_1_28.shp", 210, 0.9670213338968012);
		assertFireEvolution("shape_1_29.shp", 240, 1.7057582788815953);
		assertFireEvolution("shape_1_30.shp", 120, 1.9829366011599692);
	}
	
	private void assertFireEvolution(String shapeFilePath, int simulationTime, double predictionError) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, shapeFilePath);
		Pair<Long, Double> fireEvolution = FarsiteOutputProcessor.getFireEvolution(perimeter1File, shapeFile);
		//System.out.printf("%s Error: %s \n", shapeFilePath, Error);
		Long time = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();
		assertEquals(Long.valueOf(simulationTime), time);
		assertEquals(Double.valueOf(predictionError), error);
	}

	private void assertPredictionError(String shapeFilePath, double predictionError) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, shapeFilePath);
		Double Error = FarsiteOutputProcessor.calculateNormalizedSymmetricDifference(perimeter1File, shapeFile);
		//System.out.printf("%s Error: %s \n", shapeFilePath, Error);
		assertEquals(Double.valueOf(predictionError), Error);
	}

	private void assertPredictionError(String shapeFilePath, int simulationTime, double predictionError, int totalSimulationTime, double fitnessEvaluation) throws IOException {
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(resourcesDir, shapeFilePath);
		Pair<Long, Double> fireEvolution = FarsiteOutputProcessor.getFireEvolution(perimeter1File, shapeFile);
		//System.out.printf("%s Error: %s \n", shapeFilePath, Error);
		Long time = fireEvolution.getLeft();
		Double error = fireEvolution.getRight();
		assertEquals(Long.valueOf(simulationTime), time);
		assertEquals(Double.valueOf(predictionError), error);
		Double weightedError = FarsiteOutputProcessor.calculateWeightedPredictionError(perimeter1File, shapeFile, Long.valueOf(totalSimulationTime));
		//System.out.printf("%s Weighted Error: %s \n", shapeFilePath, weightedError);
		assertEquals(Double.valueOf(fitnessEvaluation), weightedError);
	}
	
}