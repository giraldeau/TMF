package ca.polymtl.lttng.pwm;

import java.util.ArrayList;

public class SampleSeries {
	
	public ArrayList<Double> x; 
	public ArrayList<Double> y;
	
	public SampleSeries() {
		reset();
	}
	
	public void addPoint(Double x, Double y) {
		this.x.add(x);
		this.y.add(y);
	}
	
	public void reset() {
		this.x = new ArrayList<Double>();
		this.y = new ArrayList<Double>();
	}
	
	private double[] buildArray(ArrayList<Double> a) {
		// this function sucks because we can't convert
		// Double[] to double[] 
		double[] res = new double[a.size()];
		int i = 0;
		for (Double d: a) {
			res[i] = d.doubleValue();
			i++;
		}
		return res;
	}
	
	public double[] getXSeries() {
		return buildArray(x);
	}

	public double[] getYSeries() {
		return buildArray(y);
	}
	public int size() {
		return this.x.size();
	}
	public Double getX(int index) {
		return this.x.get(index);
	}
	public Double getY(int index) {
		return this.y.get(index);
	}
	public boolean isHigh(int index) {
		if (this.y.get(index) >= 1.0) {
			return true;
		}
		return false;
	}
	public String toString() {
		String res = ""; 
		for(int i=0; i<x.size(); i++){
			res += "(" + x.get(i) + "," + y.get(i) + ")";
		}
		return res;
	}
}
