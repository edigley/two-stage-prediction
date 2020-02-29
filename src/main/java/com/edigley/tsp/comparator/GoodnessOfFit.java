package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.edigley.tsp.util.shapefile.ShapeFileWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class GoodnessOfFit implements ComparisonMethod {

	private static final Logger logger = LoggerFactory.getLogger(GoodnessOfFit.class);
	
	private File ignitionPerimeterFile;

	private MultiPolygon ignitionPerimeterMap;
	
	private boolean saveIntermediatePolygons = false;
	
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
		
		MultiPolygon predictionMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(predictionFile));
		MultiPolygon perimeterMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(perimeterFile));
		
		Double gof = calculateGoodnessOfFit(predictionMap, perimeterMap);
		return gof;
	}
	
	public Double calculateGoodnessOfFit(MultiPolygon map1, MultiPolygon map2) {
		
		/*
		try {
		File perimeter0File = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/arkadia/landscape/Per1_utm.shp");
		MultiPolygon perimeter0Map = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(perimeter0File));
		
		map1 = ShapeFileUtil.toMultiPolygon(map1.difference(perimeter0Map));
		map2 = ShapeFileUtil.toMultiPolygon(map2.difference(perimeter0Map));
		*/
		
		if (this.ignitionPerimeterMap != null) {
			map1 = ShapeFileUtil.toMultiPolygon(map1.difference(this.ignitionPerimeterMap));
			map2 = ShapeFileUtil.toMultiPolygon(map2.difference(this.ignitionPerimeterMap));
		}
		
		MultiPolygon polygonC = ShapeFileUtil.toMultiPolygon(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		MultiPolygon polygonA = ShapeFileUtil.toMultiPolygon(map1.difference(polygonC));
		Double a = polygonA.getArea();
		MultiPolygon polygonB = ShapeFileUtil.toMultiPolygon(map2.difference(polygonC));
		Double b = polygonB.getArea();

		if (saveIntermediatePolygons) {
			saveIntermediatePolygons(polygonA, polygonB, polygonC);
		}
		
		Double gof = ( c/(b+c) ) * ( c/(a+c) );
		
		return gof;	

	}

	private void saveIntermediatePolygons(MultiPolygon polygonA, MultiPolygon polygonB, MultiPolygon polygonC) {
		try {
			File outputDir = new File("/home/edigley/git/two-stage-prediction/playpen/intermediate_polygons");
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

	@Override
	public void setIgnitionPerimeterFile(File ignitionPerimeterFile) {
		this.ignitionPerimeterFile = ignitionPerimeterFile;
		try {
			Geometry geometriesPoligon = ShapeFileReader.getGeometriesPoligon(this.ignitionPerimeterFile);
			this.ignitionPerimeterMap = ShapeFileUtil.toMultiPolygon(geometriesPoligon);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setSaveIntermediatePolygons(boolean saveIntermediatePolygons) {
		this.saveIntermediatePolygons = saveIntermediatePolygons;
	}
	
	public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException {
		GoodnessOfFit comparator = new GoodnessOfFit();
		comparator.setSaveIntermediatePolygons(true);
		
		File predictionFile = new File("/home/edigley/git/two-stage-prediction/playpen/executions/cached/arkadia/execution_nsd_seed_88_1/output/shape_1_2.shp");
		File perimeterFile = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/arkadia/landscape/Per2_utm.shp");
		File perimeter0File = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/arkadia/landscape/Per1_utm.shp");
		
		MultiPolygon predictionMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(predictionFile));
		MultiPolygon perimeterMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(perimeterFile));
		MultiPolygon perimeter0Map = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(perimeter0File));
		
		File outputDir = new File("/home/edigley/git/two-stage-prediction/playpen/intermediate_polygons");
		ShapeFileWriter.save(new File(outputDir, "p2_minus_p1.shp"), ShapeFileUtil.toMultiPolygon(perimeterMap.difference(perimeter0Map)), CRS.decode(ScenarioProperties.CRS));
		ShapeFileWriter.save(new File(outputDir, "pred2_minus_p1.shp"), ShapeFileUtil.toMultiPolygon(predictionMap.difference(perimeter0Map)), CRS.decode(ScenarioProperties.CRS));
		
		comparator.calculateGoodnessOfFit(predictionMap, perimeterMap);
	}

}
