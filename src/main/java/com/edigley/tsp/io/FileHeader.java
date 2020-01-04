package com.edigley.tsp.io;

import java.util.HashMap;
import java.util.Map;

public class FileHeader {

	public String HEADER;

	public String[] values; 
	
	public int length;
	
	public Map<String, Integer> map;
	
	public FileHeader(String header) {
		this.HEADER = header;
		this.values = HEADER.trim().split("\\s+");
		length = values.length;
		
		map = new HashMap<>();
		
		for (int i = 0; i < values.length; i++) {
			map.put(values[i], i);
		}
	}
	
	@Override
	public String toString() {
		return HEADER;
	}

}
