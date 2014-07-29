package org.aksw.autosparql.tbsl.algorithm.util;

import java.util.HashMap;

import org.aksw.autosparql.tbsl.algorithm.learning.Entity;

public class Prominences extends HashMap<Entity, Double>{

	private double min = Double.MAX_VALUE;
	private double max = 0;
	
	@Override
	public Double put(Entity key, Double value) {
		//update minimum and maximum
		if(value.doubleValue() > max){
			max = value;
		}
		if(value.doubleValue() < min){
			min = value;
		}
		return super.put(key, value);
		
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	

}
