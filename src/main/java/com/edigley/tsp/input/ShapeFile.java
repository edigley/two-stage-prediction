package com.edigley.tsp.input;

import java.io.File;

public class ShapeFile {

	public static void main(String[] args) throws Exception {

		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File jonqueraOutpursDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		File desktop = new File("/home/edigley/desktop/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		
		for (int i = 1; i < 15; i++) {

			String p2ShapeName = "shape_0_" + i;
			File p2File = new File(jonqueraOutpursDir, p2ShapeName + ".shp");
			File p2PolygonFile = new File(desktop, p2ShapeName + "_polygon.shp");

			ShapeFileUtil.saveGeometryPolygon(p2File, p2PolygonFile);

			Double predictionError = ShapeFileUtil.calculatePredictionError(p1File, p2File);

			System.out.printf("(%s E %s) = %s \n", p1File.getName(), p2File.getName(), predictionError);

		}
		
	}

}
