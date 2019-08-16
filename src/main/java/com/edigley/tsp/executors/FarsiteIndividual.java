package com.edigley.tsp.executors;

import io.jenetics.Genotype;

public class FarsiteIndividual {

	public static String HEADER = "t1 t10 t100 t1000 t10000 ws wd th hh adj";
	
	public static int HEADER_LENGTH = HEADER.trim().split("\\s+").length;
	
	private int t1;
	private int t10;
	private int t100;
	private int t1000;
	private int t10000;
	private int ws;
	private int wd;
	private int th;
	private int hh;
	private int adj;

	public FarsiteIndividual(String[] params) {
		assert HEADER_LENGTH <= params.length;
		t1 = Integer.valueOf(params[0]);
		t10 = Integer.valueOf(params[1]);
		t100 = Integer.valueOf(params[2]);
		t1000 = Integer.valueOf(params[3]);
		t10000 = Integer.valueOf(params[4]);
		ws = Integer.valueOf(params[5]);
		wd = Integer.valueOf(params[6]);
		th = Integer.valueOf(params[7]);
		hh = Integer.valueOf(params[8]);
		adj = Integer.valueOf(params[9]);
	}
	
	public FarsiteIndividual(String individualAsString) {
		this(individualAsString.trim().split("\\s+"));
	}
	
	public FarsiteIndividual(Genotype<?> gt) {
		this(toStringParams(gt));
	}
	
	public static String toStringParams(Genotype<?> gt) {
		return gt.toString().replace("[", "").replace("]", "").replace(",", " ");
	}
	
	@Override
	public String toString() {
		//return t1 + " " + t10 + " " + t100 + " " + t1000 + " " + t10000 + " " + ws + " " + wd + " " + th + " " + hh + " " + adj;
		String pattern = "%3s %3s %3s %3s %3s %3s %3s %3s %3s %.1f";
		return String.format(pattern, t1, t10, t100, t1000, t10000, ws, wd, th, hh, 1+(0.1*adj));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hh;
		result = prime * result + t1;
		result = prime * result + t10;
		result = prime * result + t100;
		result = prime * result + t1000;
		result = prime * result + t10000;
		result = prime * result + th;
		result = prime * result + wd;
		result = prime * result + ws;
		result = prime * result + adj;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FarsiteIndividual other = (FarsiteIndividual) obj;
		if (t1 != other.t1)
			return false;
		if (t10 != other.t10)
			return false;
		if (t100 != other.t100)
			return false;
		if (t1000 != other.t1000)
			return false;
		if (t10000 != other.t10000)
			return false;
		if (wd != other.wd)
			return false;
		if (ws != other.ws)
			return false;
		if (th != other.th)
			return false;
		if (hh != other.hh)
			return false;
		if (adj != other.adj)
			return false;
		return true;
	}
	
}
