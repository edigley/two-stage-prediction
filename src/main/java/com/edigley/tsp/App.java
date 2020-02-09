package com.edigley.tsp;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.edigley.tsp.io.output.FarsiteOutputSaver;
import com.edigley.tsp.util.shapefile.ShapeFileReader;

/**
 * Hello world!
 *
 */
@Deprecated
public class App {
	
	public static void main(String[] args) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File farsiteOutputDir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		File layerExtentFile = new File(jonqueraPerimetersDir, "jonquera_polygon_from_layer_extent.shp");

		Integer[] representatives = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 13, 20, 29, 30, 38, 40, 46, 49, 53, 54, 55, 56, 57, 58, 62, 67, 70, 72, 81, 82, 83, 84, 85};
		
		//for (int i = 1; i <= 85; i++) {
		for (int i = 5; i <= 5; i++) {
		//for (Integer i: representatives) {
			try {
				File shapeFile = new File(farsiteOutputDir, "shape_1_" + i + ".shp");
				FarsiteOutputSaver.saveAsJPG(p1File, shapeFile, layerExtentFile, null);
				System.out.println("Successfully generated for: " + "shape_1_" + i + ".shp");
			} catch (Exception e) {
				System.err.println("Failed for: " + "shape_1_" + i + ".shp");
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main3(String[] args) throws NoSuchAuthorityCodeException, FactoryException, IOException {
		
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File dir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(dir, "shape_1_1.shp");
    	
		
		//Step 1: Create map
		MapContent map = new MapContent();
		map.setTitle("World");
		
		Style style = SLD.createLineStyle(Color.BLACK, 1);
		Layer layer = new FeatureLayer(ShapeFileReader.getFeatureSource(p1File), style);
	    layer.setTitle("jonquera_perimeter_1.shp");
		
		map.addLayer(layer);

		//Step 2: Set projection
		CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
		MapViewport vp = map.getViewport();
		vp.setCoordinateReferenceSystem(crs);

		//Step 4: Save image
		FarsiteOutputSaver.saveAsJPG(map, new File("graticules.jpg"), 800, new String[] {"BLA"});
	}
	
    public static void main2(String[] args) throws Exception {
    	
		File jonqueraPerimetersDir = new File("/home/edigley/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/");
		File dir = new File("/home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/");
		
		File p1File = new File(jonqueraPerimetersDir, "jonquera_perimeter_1.shp");
		File shapeFile = new File(dir, "shape_1_1.shp");
    	
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        //SLDParser parser = new SLDParser(styleFactory, new URL("file:///home/dwins/Projects/geoserver/data/release/styles/popshade.sld"));
        //Style style = parser.readXML()[0];
        Style style = styleFactory.getDefaultStyle();
        style = styleFactory.createStyle();
        ShapefileDataStore shapefile = new ShapefileDataStore(new URL("file:///home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/output/shape_1_1.shp"));
        shapefile = new ShapefileDataStore(new URL("file:///home/edigley/doutorado_uab/git/two-stage-prediction/playpen/fire-scenarios/jonquera/perimetres/jonquera_perimeter_1.shp"));
        
        System.out.println("---> shapefile.getTypeNames(): " + shapefile.getTypeNames()[0]);
        
        /*
        Geometry geometry = ShapeFileUtil.getGeometriesPoligon(p1File);
        Geometry geometry = ShapeFileUtil.getLastFeature(p1File);
        */
        
        FeatureSource<SimpleFeatureType, SimpleFeature> features = shapefile.getFeatureSource(shapefile.getTypeNames()[0]);

        System.out.println("---> features: " + features.getName());
        System.out.println("---> features: " + features.getInfo().getDescription());
        System.out.println("---> features: " + features.getInfo().getBounds().getLowerCorner());
        System.out.println("---> features: " + features.getInfo().getBounds().getUpperCorner());

        
        MapContext context = new DefaultMapContext(new MapLayer[]{
            new DefaultMapLayer(features, style)
        });

        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        Rectangle screenArea = new Rectangle(0, 0, 300, 300);
        ReferencedEnvelope mapArea = features.getBounds();

        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(context);
        renderer.paint(graphics, screenArea, mapArea);

        ImageIO.write(image, "png", new File("/home/edigley/states.png"));
    }
    
}
