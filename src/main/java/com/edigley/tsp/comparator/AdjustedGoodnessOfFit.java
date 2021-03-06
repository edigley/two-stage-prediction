package com.edigley.tsp.comparator;

import static com.edigley.tsp.util.shapefile.ShapeFileUtil.toMultiPolygon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class AdjustedGoodnessOfFit extends GoodnessOfFit {

	private static final Logger logger = LoggerFactory.getLogger(AdjustedGoodnessOfFit.class);
	
	public Double calculateGoodnessOfFit(Polygon map1, Polygon map2) {
		MultiPolygon polygonC = toMultiPolygon(map1.intersection(map2));
		Double c = polygonC.getArea();
		
		MultiPolygon polygonA = toMultiPolygon(map1.difference(polygonC));
		Double a = polygonA.getArea();
		MultiPolygon polygonB = toMultiPolygon(map2.difference(polygonC));
		Double b = polygonB.getArea();

		MultiPolygon map1DifferenceMap2 = toMultiPolygon(map2.difference(map1));
		MultiPolygon polygonOutOfPrediction = toMultiPolygon(map1DifferenceMap2.intersection(map2));

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
