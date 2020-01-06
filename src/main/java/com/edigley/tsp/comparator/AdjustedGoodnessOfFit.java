package com.edigley.tsp.comparator;

import java.io.File;

import org.geotools.referencing.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.io.input.ScenarioProperties;
import com.edigley.tsp.util.shapefile.ShapeFileWriter;
import com.vividsolutions.jts.geom.Polygon;

import static com.edigley.tsp.util.shapefile.ShapeFileUtil.toPolygon;

public class AdjustedGoodnessOfFit extends GoodnessOfFit {

	private static final Logger logger = LoggerFactory.getLogger(AdjustedGoodnessOfFit.class);
	
	public Double calculateGoodnessOfFit(Polygon map1, Polygon map2) {
		Polygon polygonC = toPolygon(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		Polygon polygonA = toPolygon(map1.difference(polygonC));
		Double a = polygonA.getArea();
		Polygon polygonB = toPolygon(map2.difference(polygonC));
		Double b = polygonB.getArea();

		//Polygon polygonOutOfPrediction = toPolygon(toPolygon(polygonB.difference(polygonA)).intersection(polygonB));
		Polygon map1DifferenceMap2 = toPolygon(map1.difference(map2));
		//Polygon polygonOutOfPrediction = toPolygon(map1DifferenceMap2.intersection(map2));
		Polygon polygonOutOfPrediction = toPolygon(map1DifferenceMap2.intersection(map1));
		//Polygon polygonOutOfPrediction = map1DifferenceMap2;
		
		/*
		try {
			ShapeFileWriter.save(new File("a_map1.shp"), map1, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File("a_map2.shp"), map2, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File("a_map1_difference_map2.shp"), map1DifferenceMap2, CRS.decode(ScenarioProperties.CRS));
			ShapeFileWriter.save(new File("a_polygonOutOfPrediction.shp"), polygonOutOfPrediction, CRS.decode(ScenarioProperties.CRS));
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		Double insidenessInitial = (1 - polygonOutOfPrediction.getArea()/polygonB.getArea());
		Double insideness = Math.min(1, Math.abs(insidenessInitial));
		
		System.out.println("Proportion of insideness: " + insidenessInitial + " -> " + insideness);
		
		Double gof = ( c/(b+c) ) * ( c/(a+c) ) * insideness;
		
		System.out.println("--------------------> gof: " + gof);
		
		return gof;		
	}

}
