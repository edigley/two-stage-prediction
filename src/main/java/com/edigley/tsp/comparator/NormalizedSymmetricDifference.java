package com.edigley.tsp.comparator;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edigley.tsp.entity.FarsiteExecution;
import com.edigley.tsp.util.shapefile.ShapeFileReader;
import com.edigley.tsp.util.shapefile.ShapeFileUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.TopologyException;

public class NormalizedSymmetricDifference implements ComparisonMethod {

	private static final Logger logger = LoggerFactory.getLogger(NormalizedSymmetricDifference.class);
	
	private File ignitionPerimeterFile;
	
	private MultiPolygon ignitionPerimeterMap;
	
	public Double compare(String predictionFilePath, String perimeterFilePath) throws IOException {
		return compare(new File(predictionFilePath), new File(perimeterFilePath));
	}
	
	public Double compare(File predictionFile, File perimeterFile) throws IOException {
		MultiPolygon mpA = ShapeFileUtil.toMultiPolygon(perimeterFile);

		MultiPolygon mpB = ShapeFileUtil.toMultiPolygon(predictionFile);
		
		if (this.ignitionPerimeterMap != null) {
			mpA = ShapeFileUtil.toMultiPolygon(mpA.difference(this.ignitionPerimeterMap));
			mpB = ShapeFileUtil.toMultiPolygon(mpB.difference(this.ignitionPerimeterMap));
		}
		
		/*
			Couldn't compare non-finished scenario result for individual. Error message: found non-noded intersection between LINESTRING ( 613543.571070896 4135960.1063465015, 613984.4018080924 4136107.096768829 ) and LINESTRING ( 613619.7585862151 4135985.4761876753, 613543.5710713245 4135960.1063466445 ) [ (613543.571071483, 4135960.106346697, NaN) ]
			Couldn't compare non-finished scenario result for individual. Error message: found non-noded intersection between LINESTRING ( 613169.0629641919 4135813.6105642314, 613113.5503545223 4135789.7003338896 ) and LINESTRING ( 613113.5503540069 4135789.7003336675, 613133.3923020917 4135798.2479425482 ) [ (613113.5503560981, 4135789.7003345685, NaN) ]
			There was an error when trying to save the .jpg image for prediction file: /home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/arkadia/output/shape_9_468.shp 
			com.vividsolutions.jts.geom.TopologyException: found non-noded intersection between LINESTRING ( 613543.571070896 4135960.1063465015, 613543.5710712721 4135960.106346627 ) and LINESTRING ( 613543.571070965 4135960.106346525, 613543.5710712768 4135960.1063466286 ) [ (613543.5710711413, 4135960.1063465835, NaN) ]
			520 -   8 ==> [ 10 522 ]  12  14  12  20  99   28   46  32  72  1.9  NaN     120      1      3 playpen/fire-scenarios/arkadia/output/shape_10_522.shp -> [pool-2-thread-4]
		 */
		Geometry symmetricDifference;
		try {
			symmetricDifference = mpA.symDifference(mpB);	
		} catch (TopologyException e) {
			logger.error(e.getMessage(), e);
			symmetricDifference = mpA.union(mpB);
		}
		//normalizes the symmetric difference
		double symmetricDifferenceArea = symmetricDifference.getArea();
		double normalizedSymmetricDifference = symmetricDifferenceArea / mpA.getArea();
		return normalizedSymmetricDifference;
	}

	@Override
	public Double defineAdjustmentFactor(Long effectivelySimulatedTime, Long expectedSimulatedTime) {
		Double factor = Math.max(1.0, expectedSimulatedTime / (effectivelySimulatedTime * 1.0));
		return factor;
	}
	
	@Override
	public int compare(FarsiteExecution e1, FarsiteExecution e2) {
		return e1.getFireError().compareTo(e2.getFireError());
	}

	@Override
	public void setIgnitionPerimeterFile(File ignitionPerimeterFile) {
		this.ignitionPerimeterFile = ignitionPerimeterFile;
		try {
			this.ignitionPerimeterMap = ShapeFileUtil.toMultiPolygon(ShapeFileReader.getGeometriesPoligon(this.ignitionPerimeterFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
