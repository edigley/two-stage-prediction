package com.edigley.tsp.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe counter implementation
 *
 */
@Deprecated
public class Counter {

	private int count;
	
	AtomicInteger atomicCount = new AtomicInteger(0);
	
	public synchronized int getCount() {
		return count++;
	}
	
}
