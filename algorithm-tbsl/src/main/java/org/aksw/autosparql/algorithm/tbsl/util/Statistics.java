package org.aksw.autosparql.algorithm.tbsl.util;

public class Statistics {

	public static double minMaxNorm(double value, double max, double min){
		if(max == min){
			return 1;
		}
		return (value - min) / (max - min);
	}

}
