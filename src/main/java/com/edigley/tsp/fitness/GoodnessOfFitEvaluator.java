package com.edigley.tsp.fitness;

import java.io.File;
import java.io.IOException;

import org.geotools.referencing.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.edigley.tsp.util.shapefile.ShapeFileWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GoodnessOfFitEvaluator implements IndividualEvaluator{

	private static final Logger logger = LoggerFactory.getLogger(GoodnessOfFitEvaluator.class);
	
	public Double evaluate(String gAFilePath, String gBFilePath) throws IOException {
		return evaluate(new File(gAFilePath), new File(gBFilePath));
	}
	
	public Double evaluate(File gAFile, File gBFile) throws IOException {
		MultiPolygon polygonA = (MultiPolygon) ShapeFileReader.getGeometry(gAFile);

		MultiPolygon multiPolygonB = null;
		Polygon polygonB = null;

		try {
			multiPolygonB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(gBFile);
		} catch (ClassCastException e) {
			try {
				polygonB = (Polygon) ShapeFileReader.getGeometriesPoligon(gBFile);
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + gBFile.getAbsolutePath(), e2);
			}
		}

		Geometry symmetricDifference = polygonA.symDifference((multiPolygonB != null) ? multiPolygonB : polygonB);
		
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / polygonA.getArea();
		
		return normalizedSymmetricDifference;
	}
	
	public static void main(String[] args) throws Exception {
		File resourcesDir = new File("src/test/resources/");
		File perimeter1File = new File(resourcesDir, "jonquera_perimeter_1.shp");
		
		String shapeName = "shape_1_82";
		//File shapeFilePolygon = new File(resourcesDir, shapeName + "_polygon.shp");
		//ShapeFileWriter.saveGeometryPolygon(shapeFile, shapeFilePolygon);
		
		File shapeFile = new File(resourcesDir, shapeName + ".shp");

		File map1File = new File(resourcesDir, "gof_polygon_a.shp");
		File map2File = new File(resourcesDir, "gof_polygon_b.shp");
		
		File mapAFile = new File("a.shp");
		File mapBFile = new File("b.shp");
		File mapCFile = new File("c.shp");

		System.out.println(calculateGoodnessOfFit(shapeFile, perimeter1File));		
		
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

	private static Double calculateGoodnessOfFit(File map1File, File map2File) throws IOException {
		
		Polygon map1 = ShapeFileUtil.toPolygon(ShapeFileReader.getGeometriesPoligon(map1File));
		Polygon map2 = ShapeFileUtil.toPolygon(ShapeFileReader.getGeometriesPoligon(map2File));
		
		Double gof = calculateGoodnessOfFit(map1, map2);
		return gof;
	}
	
	public static Double calculateGoodnessOfFit(Polygon map1, Polygon map2) {
		Polygon polygonC = ShapeFileUtil.toPolygon(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		Polygon polygonA = ShapeFileUtil.toPolygon(map1.difference(polygonC));
		Double a = polygonA.getArea();
		Polygon polygonB = ShapeFileUtil.toPolygon(map2.difference(polygonC));
		Double b = polygonB.getArea();

		/*
		try {
			ShapeFileWriter.save(new File("a.shp"), polygonA, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File("b.shp"), polygonB, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File("c.shp"), polygonC, CRS.decode(ScenarioProperties.CRS));
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		Double gof = ( c/(b+c) ) * ( c/(a+c) );
		
		return gof;		
	}

}
