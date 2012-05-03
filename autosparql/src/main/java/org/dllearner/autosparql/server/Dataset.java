package org.dllearner.autosparql.server;

import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.search.Search;

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
