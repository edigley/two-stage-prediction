package com.edigley.tsp.util.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.io.output.FarsiteOutputProcessor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShapeFileCalculator {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileCalculator.class);
	
	public final static double calculateAngleFrom(double obj1X, double obj1Y, double obj2X, double obj2Y) {
		double angleTarget = (double) Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if (angleTarget < 0) {
			angleTarget = 360 + angleTarget;
		}
		//return angleTarget;
		//tests
		return 360 - (((angleTarget - 90) + 180 ) % 360);
		//return Math.min(360, angleTarget + 90);
	}
	
	public final static double calculateDirection(Point starting, Point destination) {
		return calculateAngleFrom(starting.getX(), starting.getY(), destination.getX(), destination.getY());
	}
	
	public static boolean boundariesTouch(File firePerimeter, File layerExtentFile) throws IOException {
		Polygon layerExtent = (Polygon) ShapeFileReader.getGeometriesPoligon(layerExtentFile);
		// Polygon shapeInternal = (Polygon)
		// ShapeFileUtil.getGeometriesPoligon(shapeInternalPolygonFile);

		MultiPolygon mpB = null;
		Polygon pB = null;

		try {
			mpB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(firePerimeter);
			logger.info("---> mpB.getArea(): " + mpB.getArea());
		} catch (ClassCastException e) {
			try {
				pB = (Polygon) ShapeFileReader.getGeometriesPoligon(firePerimeter);
				logger.info("---> pB.getArea(): " + pB.getArea());
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + firePerimeter.getAbsolutePath(), e2);
				return false;
			}
		}

		if (mpB != null) {
			return layerExtent.getExteriorRing().isWithinDistance(mpB, 1);
		} else {
			return layerExtent.getExteriorRing().isWithinDistance(pB, 1);
		}

	}
	
	public static void main0(String[] args) throws IOException {
		
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File dir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File layerExtentFile = new File(jonqueraPerimetersDir, "jonquera_polygon_from_layer_extent.shp");
		
		File shapeInternalPolygonFile = new File(dir, "shape_11_585_polygon.shp");
		File shapeExternalPolygonFile = new File(dir, "shape_11_587_polygon.shp");

		//System.out.println(ShapeFileUtil.boundariesTouch(shapeInternalPolygonFile, layerExtentFile));
		//System.out.println(ShapeFileUtil.boundariesTouch(shapeExternalPolygonFile, layerExtentFile));
		
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_551.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_555.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_562.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_558.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_572.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_571.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_582.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_585.shp"), layerExtentFile));
		System.out.println(ShapeFileCalculator.boundariesTouch(new File(dir, "shape_11_587.shp"), layerExtentFile));
		
	}
	
	public static void main4(String[] args) throws Exception {
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File dir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File layerExtentFile = new File(jonqueraPerimetersDir, "jonquera_polygon_from_layer_extent.shp");
		
		File shapeInternalPolygonFile = new File(dir, "shape_11_585_polygon.shp");
		File shapeExternalPolygonFile = new File(dir, "shape_11_587_polygon.shp");
		
		
		Polygon layerExtent = (Polygon) ShapeFileReader.getGeometriesPoligon(layerExtentFile);
		Polygon shapeInternal = (Polygon) ShapeFileReader.getGeometriesPoligon(shapeInternalPolygonFile);
		Polygon shapeExternal = (Polygon) ShapeFileReader.getGeometriesPoligon(shapeExternalPolygonFile);
		
		System.out.println("layerExtent.getBoundary().touches(shapeInternal.getBoundary()): " + layerExtent.getBoundary().touches(shapeInternal.getBoundary()));
		System.out.println("layerExtent.getBoundary().touches(shapeExternal.getBoundary()): " + layerExtent.getBoundary().touches(shapeExternal.getBoundary()));

		System.out.println("layerExtent.touches(shapeInternal): " + layerExtent.touches(shapeInternal));
		System.out.println("layerExtent.touches(shapeExternal): " + layerExtent.touches(shapeExternal));
		
		System.out.println("shapeInternal.touches(shapeExternal): " + shapeInternal.touches(shapeExternal));
		System.out.println("shapeExternal.touches(shapeInternal): " + shapeExternal.touches(shapeInternal));
		
		System.out.println("shapeExternal.intersects(shapeInternal): " + shapeExternal.intersects(shapeInternal));
		System.out.println("shapeInternal.intersects(shapeExternal): " + shapeInternal.intersects(shapeExternal));
		
		System.out.println("layerExtent.intersects(shapeInternal): " + layerExtent.intersects(shapeInternal));
		System.out.println("layerExtent.intersects(shapeExternal): " + layerExtent.intersects(shapeExternal));
		
		System.out.println("layerExtent.getEnvelope().touches(shapeExternal.getEnvelope()): " + layerExtent.getEnvelope().touches(shapeExternal.getEnvelope()));
		System.out.println("layerExtent.getEnvelope().touches(shapeInternal.getEnvelope()): " + layerExtent.getEnvelope().touches(shapeInternal.getEnvelope()));

		System.out.println("layerExtent.getEnvelope().covers(shapeExternal.getEnvelope()): " + layerExtent.getEnvelope().covers(shapeExternal.getEnvelope()));
		System.out.println("layerExtent.getEnvelope().covers(shapeInternal.getEnvelope()): " + layerExtent.getEnvelope().covers(shapeInternal.getEnvelope()));
		
		System.out.println("shapeExternal.getEnvelope().covers(shapeInternal.getEnvelope()): " + shapeExternal.getEnvelope().covers(shapeInternal.getEnvelope()));
		
		System.out.println("layerExtent.getExteriorRing().touches(shapeExternal.getExteriorRing()): " + layerExtent.getExteriorRing().touches(shapeExternal.getExteriorRing()));
		System.out.println("layerExtent.getExteriorRing().touches(shapeInternal.getExteriorRing()): " + layerExtent.getExteriorRing().touches(shapeInternal.getExteriorRing()));

		System.out.println("layerExtent.getExteriorRing().intersects(shapeExternal.getExteriorRing()): " + layerExtent.getExteriorRing().intersects(shapeExternal.getExteriorRing()));
		System.out.println("layerExtent.getExteriorRing().intersects(shapeInternal.getExteriorRing()): " + layerExtent.getExteriorRing().intersects(shapeInternal.getExteriorRing()));
		
		System.out.println("layerExtent.getExteriorRing().covers(shapeExternal.getExteriorRing()): " + layerExtent.getExteriorRing().covers(shapeExternal.getExteriorRing()));
		System.out.println("layerExtent.getExteriorRing().covers(shapeInternal.getExteriorRing()): " + layerExtent.getExteriorRing().covers(shapeInternal.getExteriorRing()));

		Polygon differenceExternal = (Polygon) layerExtent.difference(shapeExternal);
		ShapeFileUtil.save(new File(dir, "15_differenceExternal.shp"), differenceExternal, null);
		
		Geometry convexHull = (Polygon)shapeExternal.convexHull();
		Polygon differenceConvexHull = (Polygon)layerExtent.difference(convexHull);
		ShapeFileUtil.save(new File(dir, "15_differenceExternal_convex_hull.shp"), differenceConvexHull, null);
		
		Polygon differenceInternal = (Polygon) layerExtent.difference(shapeInternal);
		ShapeFileUtil.save(new File(dir, "15_differenceInternal.shp"), differenceInternal, null);
		
		System.out.println(differenceExternal.getNumGeometries());
		System.out.println(differenceInternal.getNumGeometries());
		System.out.println(differenceConvexHull.getNumGeometries());
		System.out.println(layerExtent.touches(convexHull));
		System.out.println(layerExtent.intersects(convexHull));
		System.out.println(layerExtent.getEnvelope().touches(convexHull.getEnvelope()));
		System.out.println(layerExtent.getExteriorRing().isWithinDistance(convexHull, 1));
		System.out.println(layerExtent.getExteriorRing().isWithinDistance(shapeExternal, 0.01));
		System.out.println(shapeInternal.getExteriorRing().getLength());
		System.out.println(layerExtent.getExteriorRing().isWithinDistance(shapeInternal, 1));
				
		/*
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		File shapeFileInternal = new File(dir, "shape_11_585.shp");
		ShapeFileUtil.saveGeometryPolygon(shapeFile, shapePolygonFile);
		
		MultiPolygon shape = (MultiPolygon) ShapeFileUtil.getGeometry(shapePolygonFile);
		
		Double Error = ShapeFileUtil.calculatePredictionError(p1File, shapeFile);		
		System.out.println("Error: " + Error);
		*/
	}
	
	public static void main(String[] args) throws Exception {
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File dir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(dir, "shape_1_1.shp");
		
		File shapePolygonFile = new File(dir, "shape_1_1_polygon.shp");
		
		ShapeFileWriter.saveGeometryPolygon(shapeFile, shapePolygonFile);
		
		MultiPolygon shape = (MultiPolygon) ShapeFileReader.getGeometry(shapePolygonFile);
		
		Double Error = FarsiteOutputProcessor.calculateNormalizedSymmetricDifference(p1File, shapeFile);		
		System.out.println("Error: " + Error);
	}
	
	public static void main2(String[] args) throws Exception {

		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File jonqueraOutpursDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		File desktop = new File("/home/edigley/desktop/");
		
		File p0File = new File(jonqueraPerimetersDir, "jonquera_ignition.shp");
		
		Point p0 = (Point) ShapeFileReader.getGeometry(p0File);
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		
		MultiPolygon pA = (MultiPolygon) ShapeFileReader.getGeometry(p1File);
		Point pACentroid = printCentroidStuffs(pA);
		
		String pBShapeName = "shape_1_41";
		pBShapeName = "shape_0_5";
		pBShapeName = "shape_0_8";
		pBShapeName = "shape_0_14"; // 329.8547324059052
		pBShapeName = "shape_0_19"; // 314.078653226481
		pBShapeName = "shape_0_16"; // 289.94663726185723
		pBShapeName = "shape_0_28"; // 352.8814842028899
		pBShapeName = "shape_0_31"; // 357.22410931655156
		pBShapeName = "shape_0_34"; // 353.95016710759313
		pBShapeName = "shape_1_41"; // 359.93054910251044
		pBShapeName = "shape_1_42"; // 305.8367401765683
		pBShapeName = "shape_1_43"; // 327.1263705950911
		pBShapeName = "shape_1_51"; // 348.08190867538417
		File pBFile = new File(jonqueraOutpursDir, pBShapeName + ".shp");
		File pBPolygonFile = new File(desktop, pBShapeName + "_polygon.shp");
		ShapeFileWriter.saveGeometryPolygon(pBFile, pBPolygonFile);
		
		MultiLineString pBShape = (MultiLineString) ShapeFileReader.getGeometry(pBFile);
		System.out.println("pBShape.getDimension(): " + pBShape.getDimension());
		Feature pBFeature = (Feature) ShapeFileReader.getFeature(pBFile);
		List<Feature> allFeatures = ShapeFileReader.getAllFeatures(pBFile);
		System.out.println("pBFeature.getUserData(): " + pBFeature.getUserData());
		System.out.println("pBFeature.getProperties(): " + pBFeature.getProperties());
		
		allFeatures.stream().forEach(f -> 
			f.getProperties("Elapsed_Mi").forEach(
				p -> System.out.println("------> " + Double.valueOf(p.getValue().toString()) + " - " + p.getType())
			)
		);
		pBFeature.getProperties().forEach(p -> System.out.println("---> " + p.getName()));
		
		Collection<Property> property = pBFeature.getProperties("Elapsed_Mi");
		System.out.println("--> pBFeature.getProperties(\"Elapsed_Mi\"): " + property);
		
		for (Property p : property) {
			System.out.println("---> p: " + p);
			System.out.println("---> p.getType(): " + p.getType());
			System.out.println("---> p.getUserData(): " + p.getUserData());
			System.out.println("---> p.getValue(): " + p.getValue());
			System.out.println("---> p.getDescriptor(): " + p.getDescriptor());
		}
		
		
		System.exit(1);
		
		MultiPolygon pB = (MultiPolygon) ShapeFileReader.getGeometry(pBPolygonFile);
		Point pBCentroid = printCentroidStuffs(pB);
		
		
		System.out.println(" calculateDirection A B: " + calculateDirection(pACentroid, pBCentroid));
		System.out.println(" calculateDirection 0 A: " + calculateDirection(p0, pACentroid));
		System.out.println(" calculateDirection 0 B: " + calculateDirection(p0, pBCentroid));
		
		/*
		final GeodeticCalculator calc = new GeodeticCalculator();
		//pACentroid.
		Point2D startingPoint = new Point2D.Double(pACentroid.getX(), pACentroid.getY());
		Point2D destinationPoint = new Point2D.Double(pBCentroid.getX(), pBCentroid.getY());
		calc.setStartingGeographicPoint(startingPoint);
		calc.setDestinationGeographicPoint(destinationPoint);

		System.out.println("----> calc.getAzimuth(): " + calc.getAzimuth());
		
		*/
		System.exit(1);
		
		for (int i = 1; i < 15; i++) {

			String p2ShapeName = "shape_0_" + i;
			File p2File = new File(jonqueraOutpursDir, p2ShapeName + ".shp");
			File p2PolygonFile = new File(desktop, p2ShapeName + "_polygon.shp");

			ShapeFileWriter.saveGeometryPolygon(p2File, p2PolygonFile);

			Double predictionError = FarsiteOutputProcessor.calculateNormalizedSymmetricDifference(p1File, p2File);

			System.out.printf("(%s E %s) = %s \n", p1File.getName(), p2File.getName(), predictionError);

		}
		
	}

	private static Point printCentroidStuffs(MultiPolygon pB) {
		System.out.println("pB.getFactory().getSRID(): " + pB.getFactory().getSRID());
		System.out.println("---> pB.getSRID(): " + pB.getSRID());
		System.out.println("---> pB.getArea(): " + pB.getArea());
		Point pBCentroid = pB.getCentroid();
		
		System.out.println("---> pB.getCentroid().getCoordinates().length: " + pBCentroid.getCoordinates().length);
		System.out.println("---> pB.getCentroid().getCoordinates()[0]: " + pBCentroid.getCoordinates()[0]);
		System.out.println("---> pB.getCentroid().getCoordinates()[0].x: " + pBCentroid.getCoordinates()[0].x);
		System.out.println("---> pB.getCentroid().getCoordinates()[0].y: " + pBCentroid.getCoordinates()[0].y);
		System.out.println("---> pB.getCentroid().getCoordinates()[0].z: " + pBCentroid.getCoordinates()[0].z);
		return pBCentroid;
	}

}
