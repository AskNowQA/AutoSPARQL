package org.aksw.autosparql.tbsl.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Interval {
	
	private double lowerBoundary;
	private double upperBoundary;
	
	private Map<String, Double> entries;
	
	public Interval(double lowerBoundary, double upperBoundary) {
		this.lowerBoundary = lowerBoundary;
		this.upperBoundary = upperBoundary;
		
		entries = new HashMap<String, Double>();
	}
	
	public boolean addToInterval(Entry<String, Double> entry){
		return addToInterval(entry.getKey(), entry.getValue());
	}
	
	public boolean addToInterval(String key, double value){
		if(value > lowerBoundary && value <= upperBoundary){
			entries.put(key, value);
			return true;
		}
		return false;
	}
	
	public int getSize(){
		return entries.size();
	}

	public double getLowerBoundary() {
		return lowerBoundary;
	}

	public double getUpperBoundary() {
		return upperBoundary;
	}

}
