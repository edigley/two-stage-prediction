package com.edigley.tsp.util.shapefile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Collections2;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

public class ShapeFileUtil {

	private static final Logger logger = LoggerFactory.getLogger(ShapeFileUtil.class);

	public static void describe(File file) throws Exception {
		logger.info("describe.fileName: " + file.getAbsolutePath());

		Feature feature = ShapeFileReader.getLastFeature(file);
		logger.info("---> getPolygon.feature.getType(): " + feature.getType());
		logger.info("---> feature.getProperties().size(): " + feature.getProperties().size());
		feature.getProperties().stream().forEach(p -> logger.info(p.getName().toString()));
		GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
		Object polygon = sourceGeometry.getValue();

		logger.info("---> sourceGeometry.name: " + sourceGeometry.getName());
		logger.info("---> sourceGeometry.descriptor: " + sourceGeometry.getDescriptor());
		logger.info("---> sourceGeometry.identifier: " + sourceGeometry.getIdentifier());
		logger.info("---> sourceGeometry.type: " + sourceGeometry.getType());
		logger.info("---> sourceGeometry.userData: " + sourceGeometry.getUserData());
		logger.info("---> sourceGeometry.getDescriptor().getCoordinateReferenceSystem(): "
				+ sourceGeometry.getDescriptor().getCoordinateReferenceSystem());
		logger.info("---> sourceGeometry.bounds: " + sourceGeometry.getBounds());
		// Point centroid = ((GeometryAttributeImpl) sourceGeometry).getCentroid();
		// logger.info("---> centroid: " + centroid.getDimension());
	}

	public static MultiPolygon wrapInAMultiPolygon(Polygon polygon) {
		GeometryFactory gf = new GeometryFactory();

		Polygon[] polys = new Polygon[1];
		polys[0] = polygon;

		MultiPolygon multiPolygon = gf.createMultiPolygon(polys);
		return multiPolygon;
	}

	public static Polygon toPolygonBKP(Geometry geometry) {
		try {
			return (Polygon) geometry;
		} catch (ClassCastException e) {
			try {
				MultiLineString mls = (MultiLineString) geometry;
				return (Polygon) mls.convexHull();
			} catch (ClassCastException e2) {
				try {
					MultiPolygon mp = (MultiPolygon) geometry;
					int numGeometries = mp.getNumGeometries();
					List<Geometry> geometries = new ArrayList<Geometry>(numGeometries);
					for (int i = 0; i < numGeometries; i++) {
						geometries.add(mp.getGeometryN(i));
					}
					Geometry biggestGeometry = geometries.stream()
							.sorted(Comparator.comparingDouble(Geometry::getArea).reversed()).findFirst().get();
					return (Polygon) biggestGeometry;
				} catch (ClassCastException e3) {
					try {
						Point p = (Point) geometry;
						return (Polygon)p.buffer(0.0000001);
					} catch (ClassCastException e4) {
						//LineString ls = (LineString) geometry;
						//return (Polygon) ls.convexHull();
						//return (Polygon)ls.buffer(0.0000001);
						return null;
					}
					
				}
			}
		}
	}

	public static MultiPolygon toMultiPolygon(Geometry geometry) {
		try {
			return wrapInAMultiPolygon((Polygon) geometry);
		} catch (ClassCastException e) {
			try {
				MultiLineString mls = (MultiLineString) geometry;
				return wrapInAMultiPolygon((Polygon) mls.convexHull());
			} catch (ClassCastException e2) {
				try {
					MultiPolygon mp = (MultiPolygon) geometry;
					return mp;
				} catch (ClassCastException e3) {
					try {
						GeometryCollection gc = (GeometryCollection) geometry;
						//return wrapInAMultiPolygon((Polygon) gc.getNumGeometries());
						
						GeometryFactory gf = new GeometryFactory();

						int numGeometries = gc.getNumGeometries();
						
						ArrayList<Polygon> polysA = new ArrayList<>();
						for (int i = 0; i < numGeometries; i++) {
							try {
								//polys[i] = toPolygonBKP(gc.getGeometryN(i));
								Polygon polygonBKP = toPolygonBKP(gc.getGeometryN(i));
								if (polygonBKP != null) {
									polysA.add(polygonBKP);
								}
							} catch (TopologyException te) {
								//FIXME silently ignore any further exception
							}
						}

						Polygon[] polys = new Polygon[polysA.size()];
						for (int i = 0; i < polysA.size(); i++) {
							polys[i] = polysA.get(i);
						}
						MultiPolygon multiPolygon = gf.createMultiPolygon(polys);
						
						return multiPolygon;
					} catch (ClassCastException e4) {
						e4.printStackTrace();
						throw e4;
					}
				}
			}
		}
	}
	
	public static MultiPolygon toMultiPolygon(File shapeFile) throws IOException {
		Geometry geometry = (Geometry) ShapeFileReader.getGeometriesPoligon(shapeFile);
		//Polygon polygon = toPolygon(geometry);
		//MultiPolygon multiPolygonB = wrapInAMultiPolygon(polygon);
		MultiPolygon multiPolygonB = toMultiPolygon(geometry);
		return multiPolygonB;

		/*
		try {
			multiPolygonB = (MultiPolygon) ShapeFileReader.getGeometriesPoligon(shapeFile);
		} catch (ClassCastException e) {
			try {
				Polygon polygonB = (Polygon) ShapeFileReader.getGeometriesPoligon(shapeFile);
				multiPolygonB = wrapInAMultiPolygon(polygonB);
			} catch (ClassCastException e2) {
				logger.warn("Couldn't cast shape file to Polygon: " + shapeFile.getAbsolutePath(), e2);
			}
		}
		*/
	}

}
