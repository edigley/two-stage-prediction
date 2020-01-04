package com.edigley.tsp.util;

/**
 * Thread-safe counter implementation
 *
 */
public class ParserUtil {

	public static Double parseDouble(double value) {
		return Double.parseDouble(String.format("%.6f", value).replace(",", "."));
	}
	
	public static Double parseDouble(String value) {
		return parseDouble(Double.valueOf(value));
	}
	
}
