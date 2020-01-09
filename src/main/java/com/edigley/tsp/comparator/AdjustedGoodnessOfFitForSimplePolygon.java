package com.edigley.tsp.comparator;

import static com.edigley.tsp.util.shapefile.ShapeFileUtil.toPolygonBKP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Polygon;

public class AdjustedGoodnessOfFitForSimplePolygon extends GoodnessOfFit {

	private static final Logger logger = LoggerFactory.getLogger(AdjustedGoodnessOfFitForSimplePolygon.class);
	
	public Double calculateGoodnessOfFit(Polygon map1, Polygon map2) {
		Polygon polygonC = toPolygonBKP(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		Polygon polygonA = toPolygonBKP(map1.difference(polygonC));
		Double a = polygonA.getArea();
		Polygon polygonB = toPolygonBKP(map2.difference(polygonC));
		Double b = polygonB.getArea();

		Polygon map1DifferenceMap2 = toPolygonBKP(map2.difference(map1));
		Polygon polygonOutOfPrediction = toPolygonBKP(map1DifferenceMap2.intersection(map2));

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
		
		
		double outsideness = polygonOutOfPrediction.getArea()/map2.getArea();
		Double insidenessInitial = (1 - outsideness);
		Double insideness = Math.min(1, Math.abs(insidenessInitial));
		
		System.out.println("Proportion of insideness: " + insidenessInitial + " -> " + insideness);
		
		Double gof = ( c/(b+c) ) * ( c/(a+c) ) * insideness;
		
		System.out.println("--------------------> gof: " + gof);
		
		return gof;		
	}

}
