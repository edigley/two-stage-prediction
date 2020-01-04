package com.edigley.tsp.entity;

import com.edigley.tsp.io.FileHeader;

import io.jenetics.Genotype;

public class FarsiteIndividual {

	public static final FileHeader header = new FileHeader("t1 t10 t100 t1000 t10000 ws wd th hh adj");

	public static final int PARAMS_START_POS = 0;
	
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
		assert header.length <= params.length;
		t1 = getGene(params, "t1");
		t10 = getGene(params, "t10");
		t100 = getGene(params, "t100");
		t1000 = getGene(params, "t1000");
		t10000 = getGene(params, "t10000");
		ws = getGene(params, "ws");
		wd = getGene(params, "wd");
		th = getGene(params, "th");
		hh = getGene(params, "hh");
		adj = getGene(params, "adj");
	}

	private Integer getGene(String[] params, String gene) {
		String param = params[PARAMS_START_POS + header.map.get(gene)];
		
		if (gene.equals("adj")) {
			return convertAdjustmentFactorToInt(param);
		}
		
		return Integer.valueOf(param);
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
	
	public static Double convertAdjustmentFactorToDouble(int adjI) {
		//convert from range [-9 to 9] to [0.1 to 1.9]
		double adjD = 1 + (0.1 * adjI);
		return adjD;
	}
	
	public static int convertAdjustmentFactorToInt(String adjS) {
		assert adjS.length() == 3;
		//convert from range [0.1 to 1.9] to [-9 to 9] 
		String[] splitValue = adjS.split("\\.");
		//System.out.println("splitValue: " + Arrays.toString(splitValue));
		assert splitValue.length == 2;
		if (splitValue.length == 2) {
			int integerPart = Integer.valueOf(splitValue[0]);
			int decimalPart = Integer.valueOf(splitValue[1]);
			return (integerPart == 1) ? decimalPart : (decimalPart - 10);
		} else {
			return Integer.valueOf(adjS);
		}
	}
	
	public static void main(String[] args) {
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.1"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.2"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.3"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.4"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.5"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.6"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.7"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.8"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("0.9"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.0"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.1"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.2"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.3"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.4"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.5"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.6"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.7"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.8"));
		System.out.printf("%s\n", convertAdjustmentFactorToInt("1.9"));
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
		/*result = prime * result + t10000;*/
		result = prime * result + th;
		result = prime * result + wd;
		result = prime * result + ws;
		/*result = prime * result + adj;*/
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
		/*if (t10000 != other.t10000)
			return false;*/
		if (wd != other.wd)
			return false;
		if (ws != other.ws)
			return false;
		if (th != other.th)
			return false;
		if (hh != other.hh)
			return false;
		/*if (adj != other.adj)
			return false;*/
		return true;
	}

	@Override
	public String toString() {
		double adj = convertAdjustmentFactorToDouble(this.adj); // convert the adjustment factor to decimal ranging from 0.1 to 1.9
		String pattern = "%3s %3s %3s %3s %3s %4s %4s %3s %3s  %.1f";
		return String.format(pattern, t1, t10, t100, t1000, t10000, ws, wd, th, hh, adj);
	}
	
}
