package com.edigley.tsp.comparator;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class NormalizedSymmetricDifferenceTest extends ComparisonMethodTest {

	@Before
	public void setUp( ) {
		 comparator = new NormalizedSymmetricDifference();
	}
	
	void assertComparisonBetweenTheSameShape(String shapeFilePath) throws IOException {
		assertComparison(shapeFilePath, shapeFilePath, 0.0);
	}
	
	void assertComparisonBetweenConcentricCircles(int predictionRadius, int perimeterRadius, double nsd) throws IOException {
		String prediction = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + predictionRadius + ".shp";
		String perimeter = "jonquera_ignition_buffers/jonquera_ignition_buffer_" + perimeterRadius + ".shp";
		assertComparison(prediction, perimeter, nsd);
	}
	
	@Test
	public void testCalculateNSDForTheSameFile() throws Exception {
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_100.shp"); 
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_200.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_500.shp"); 
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_1000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_2000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_3000.shp");
		assertComparisonBetweenTheSameShape("jonquera_ignition_buffers/jonquera_ignition_buffer_4000.shp");
		assertComparisonBetweenTheSameShape("shape_1_1.shp");
		assertComparisonBetweenTheSameShape("shape_1_3.shp");
	}
	
	@Test
	public void testCalculateNSDForConcentricCircles() throws Exception {
		
		// prediction equals to the real perimeter
		assertComparisonBetweenConcentricCircles(100, 100, 0.0);
		
		// prediction smaller than the real perimeter
		assertComparisonBetweenConcentricCircles(100, 200, 0.749999999999681);
		assertComparisonBetweenConcentricCircles(100, 300, 0.8888888888887944);
		assertComparisonBetweenConcentricCircles(100, 400, 0.937500000000023);
		assertComparisonBetweenConcentricCircles(100, 500, 0.9599999999999943);
		
		// prediction larger than the real perimeter
		assertComparisonBetweenConcentricCircles(200, 100,  2.999999999994897);
		assertComparisonBetweenConcentricCircles(300, 100,  7.999999999992345);
		assertComparisonBetweenConcentricCircles(400, 100, 15.000000000005896);
		assertComparisonBetweenConcentricCircles(500, 100, 23.999999999996422);

		// prediction ranging from smaller to larger perimeters
		assertComparisonBetweenConcentricCircles(100,  1000,  0.9899999999999964);
		assertComparisonBetweenConcentricCircles(200,  1000,  0.9600000000000367);
		assertComparisonBetweenConcentricCircles(300,  1000,  0.9100000000000443);
		assertComparisonBetweenConcentricCircles(400,  1000,  0.8399999999998837);
		assertComparisonBetweenConcentricCircles(500,  1000,  0.749999999999946);
		assertComparisonBetweenConcentricCircles(600,  1000,  0.6400000000001542);
		assertComparisonBetweenConcentricCircles(700,  1000,  0.5099999999999092);
		assertComparisonBetweenConcentricCircles(800,  1000,  0.3600000000000233);
		assertComparisonBetweenConcentricCircles(900,  1000,  0.19000000000036707);
		assertComparisonBetweenConcentricCircles(1000, 1000,  0.0);
		assertComparisonBetweenConcentricCircles(1100, 1000,  0.21000000000001284);
		assertComparisonBetweenConcentricCircles(1200, 1000,  0.44000000000028494);
		assertComparisonBetweenConcentricCircles(1300, 1000,  0.6899999999998485);
		assertComparisonBetweenConcentricCircles(1400, 1000,  0.9599999999998867);
		assertComparisonBetweenConcentricCircles(1500, 1000,  1.250000000000174);
		assertComparisonBetweenConcentricCircles(1600, 1000,  1.5599999999995717);
		assertComparisonBetweenConcentricCircles(1700, 1000,  1.890000000000273);
		assertComparisonBetweenConcentricCircles(1800, 1000,  2.239999999999964);
		assertComparisonBetweenConcentricCircles(1900, 1000,  2.6100000000000403);
		assertComparisonBetweenConcentricCircles(2000, 1000,  3.0000000000000258);
		assertComparisonBetweenConcentricCircles(3000, 1000,  7.9999999999993925);
		assertComparisonBetweenConcentricCircles(4000, 1000, 14.999999999999496);
		assertComparisonBetweenConcentricCircles(5000, 1000, 23.99999999999938);
		
	}

	@Test
	public void testCalculateNSDForRealFirePerimeters() throws Exception {
		assertComparison("shape_1_3.shp", "jonquera_perimeter_1.shp", 0.9148345582325907);
	}
	
}

/*

#https://rdrr.io/snippets/
library(ggplot2)


dfGoF <- read.table( header=T, text='
prediction  fitness
100  0.010000000000003588
200  0.039999999999963315
300  0.08999999999995574 
400  0.16000000000011635 
500  0.25000000000005385 
600  0.3599999999998458
700  0.49000000000009075 
800  0.6399999999999768  
900  0.8099999999996329  
1000 1.0                 
1100 0.8264462809917268  
1200 0.6944444444443071  
1300 0.591715976331414  
1400 0.5102040816326825  
1500 0.44444444444441006 
1600 0.39062500000006534 
1700 0.3460207612456421  
1800 0.3086419753086454  
1900 0.2770083102493044  
2000 0.2499999999999984  
3000 0.11111111111111861 
4000 0.06250000000000197 
5000 0.04000000000000099 
')

plot(dfGoF$prediction, dfGoF$fitness, type='l', xlab='Prediction Radius', ylab='Fitness', main='Goodness of Fit', xlim=c(100,2000))

dfNSD <- read.table( header=T, text='
prediction  fitness
100   0.9899999999999964
200   0.9600000000000367
300   0.9100000000000443
400   0.8399999999998837
500   0.749999999999946
600   0.6400000000001542
700   0.5099999999999092
800   0.3600000000000233
900   0.19000000000036707
1000  0.0
1100  0.21000000000001284
1200  0.44000000000028494
1300  0.6899999999998485
1400  0.9599999999998867
1500  1.250000000000174
1600  1.5599999999995717
1700  1.890000000000273
1800  2.239999999999964
1900  2.6100000000000403
2000  3.0000000000000258
3000  7.9999999999993925
4000 14.999999999999496
5000 23.99999999999938
')

plot(dfNSD$prediction, dfNSD$fitness, type='l', xlab='Prediction Radius', ylab='Fitness', main='Normalized Symmetric Difference', xlim=c(100,2000), ylim=c(0,3.5))


*/