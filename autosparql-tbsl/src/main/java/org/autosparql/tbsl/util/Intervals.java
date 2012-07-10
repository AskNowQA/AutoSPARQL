package org.autosparql.tbsl.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.autosparql.tbsl.model.Interval;

public class Intervals {
	
	@SuppressWarnings("unchecked")
	public static <T extends Number> Interval[] aggregate(Map<String, Set<T>> data, int nrOfIntervals){
		Map<String, T> sample = sampleNumbers(data);
		if(sample.entrySet().iterator().next().getValue() instanceof Double){
			return aggregateDoubles((Map<String, Double>) sample, nrOfIntervals);
		}
		return null;
	}
	
	
	public static Interval[] aggregateDoubles(Map<String, Double> data, int nrOfIntervals){
		//find maximum
		Double max = Double.MIN_VALUE;
		for(Entry<String, Double> entry : data.entrySet()){
			if(entry.getValue() > max){
				max = entry.getValue();
			}
		}		
		//create intervals
		double intervalSize = findNiceUpperBound(max) / nrOfIntervals;
		Interval[] intervals = new Interval[nrOfIntervals];
		double lowerBoundary = Double.MIN_VALUE;
		for(int i = 0; i < nrOfIntervals; i++){
			double upperBoundary = intervalSize * (i+1);
			intervals[i] = new Interval(lowerBoundary, upperBoundary);
			lowerBoundary = upperBoundary;
		}
		//aggregate data by intervals
		for(Entry<String, Double> entry : data.entrySet()){
			for(Interval interval : intervals){
				if(interval.addToInterval(entry)){
					break;
				}
			}
		}
		return intervals;
	}
	
	public static <T extends Number> Map<String, T> sampleNumbers(Map<String, Set<T>> data){
		Map<String, T> sample = new HashMap<String, T>();
		for(Entry<String, Set<T>> entry : data.entrySet()){
			sample.put(entry.getKey(), entry.getValue().iterator().next());
		}
		return sample;
	}
	
	public static  Map<String, Object> sample(Map<String, Set<Object>> data){
		Map<String, Object> sample = new HashMap<String, Object>();
		for(Entry<String, Set<Object>> entry : data.entrySet()){
			sample.put(entry.getKey(), entry.getValue().iterator().next());
		}
		return sample;
	}
	
	private static double findNiceUpperBound(double value){
		int exponent = 0;
		double base = 10;
		while(true){
			double potenz = Math.pow(base, exponent);
			if(potenz/value >= 1){
				return Math.ceil(value / potenz) * potenz;
			}
			exponent++;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(findNiceUpperBound(880));
	}

}
