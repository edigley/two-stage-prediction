package com.edigley.tsp.input;

import java.io.File;

import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public class ShapeFile {

	public static void main(String[] args) throws Exception {

		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File jonqueraOutpursDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		File desktop = new File("/home/edigley/desktop/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		
		String p2ShapeName = "shape_0_1";
		File p2File = new File(jonqueraOutpursDir, p2ShapeName + ".shp");
		File p2PolygonFile = new File(desktop, p2ShapeName + "_polygon.shp");
		
		MultiPolygon pA = (MultiPolygon) ShapeFileUtil.getGeometry(p1File);
		//MultiLineString l2 = (MultiLineString) getGeometry(p2FileName);
		//MultiLineString lB = (MultiLineString) getGeometriesPoligon(p2FileName);

		System.out.println("---> pA.getArea(): " + pA.getArea());
		//System.out.println("---> lB.getArea(): " + lB.getArea());

		//GeometryFactory gf = new GeometryFactory();
		//Polygon pB = gf.createPolygon(lB.getCoordinates());
		MultiPolygon pB = (MultiPolygon) ShapeFileUtil.getGeometriesPoligon(p2File);

		System.out.println("---> pB.getArea(): " + pB.getArea());

		// GeometryFunction functions = new GeometryFunction();
		// GeometryFunctions
		// MultiPolygon p = (MultiPolygon)
		// getPolygon("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp");
		Geometry symDiff = pA.symDifference(pB);
		System.out.println("---> (pB symDiff pA).getArea(): " + symDiff.getArea());
		System.out.println("---> predictionError: " + symDiff.getArea() / pA.getArea());

		Feature p1Feature = ShapeFileUtil.getFirstFeature(p1File);
		CoordinateReferenceSystem crs = p1Feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
		ShapeFileUtil.save(p2PolygonFile, pB, crs);

	}

}
