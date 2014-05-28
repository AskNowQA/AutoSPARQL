package org.aksw.autosparql.server;

import org.aksw.autosparql.server.search.Search;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;

public class Dataset {
	
	private Search searchIndex;
	private SPARQLEndpointEx endpoint;
	
	public Dataset(Search searchIndex, SPARQLEndpointEx endpoint) {
		this.searchIndex = searchIndex;
		this.endpoint = endpoint;
	}
	
	public Search getSearchIndex() {
		return searchIndex;
	}
	
	public SPARQLEndpointEx getEndpoint() {
		return endpoint;
	}
	
	@Override
	public String toString() {
		return "[Dataset:\nEndpoint: " + endpoint.getURL() + "\nIndex: " + searchIndex.getClass() + "]"; 
	}

}
