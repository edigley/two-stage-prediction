package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.geotools.referencing.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.edigley.tsp.util.shapefile.ShapeFileWriter;
import com.vividsolutions.jts.geom.Polygon;

public class GoodnessOfFit implements ComparisonMethod {

	private static final Logger logger = LoggerFactory.getLogger(GoodnessOfFit.class);
	
	public Double compare(String predictionFile, String perimeterFile) throws IOException {
		return compare(new File(predictionFile), new File(perimeterFile));
	}
	
	public Double compare(File predictionFile, File perimeterFile) throws IOException {
		return calculateGoodnessOfFit(predictionFile, perimeterFile);
	}
	
	@Override
	public Double defineAdjustmentFactor(Long effectivelySimulatedTime, Long expectedSimulatedTime) {
		Double factor = Math.min(1.0, effectivelySimulatedTime / ( expectedSimulatedTime * 1.0));
		return factor;
	}
	
	Double calculateGoodnessOfFit(File predictionFile, File perimeterFile) throws IOException {
		
		Polygon predictionMap = ShapeFileUtil.toPolygon(ShapeFileReader.getGeometriesPoligon(predictionFile));
		Polygon perimeterMap = ShapeFileUtil.toPolygon(ShapeFileReader.getGeometriesPoligon(perimeterFile));
		
		Double gof = calculateGoodnessOfFit(predictionMap, perimeterMap);
		return gof;
	}
	
	public Double calculateGoodnessOfFit(Polygon map1, Polygon map2) {
		Polygon polygonC = ShapeFileUtil.toPolygon(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		Polygon polygonA = ShapeFileUtil.toPolygon(map1.difference(polygonC));
		Double a = polygonA.getArea();
		Polygon polygonB = ShapeFileUtil.toPolygon(map2.difference(polygonC));
		Double b = polygonB.getArea();

		saveIntermediatePolygons(polygonA, polygonB, polygonC);
		
		Double gof = ( c/(b+c) ) * ( c/(a+c) );
		
		return gof;		
	}

	private void saveIntermediatePolygons(Polygon polygonA, Polygon polygonB, Polygon polygonC) {
		try {
			File outputDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output");
			ShapeFileWriter.save(new File(outputDir, "a.shp"), polygonA, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File(outputDir, "b.shp"), polygonB, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File(outputDir, "c.shp"), polygonC, CRS.decode(ScenarioProperties.CRS));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compare(FarsiteExecution e1, FarsiteExecution e2) {
		return e2.getFireError().compareTo(e1.getFireError());
	}

}
