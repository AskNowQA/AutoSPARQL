package org.aksw.autosparql.tbsl.algorithm.graph;

import org.apache.log4j.Logger;

public class SPARQLQuery2GraphConverter {
	
	private static final Logger logger = Logger.getLogger(SPARQLQuery2GraphConverter.class);
	private static final String COLOR = "white";
	
	private String endpointURL;
	
	public SPARQLQuery2GraphConverter(String endpointURL) {
		this.endpointURL = endpointURL;
	}

	
}
