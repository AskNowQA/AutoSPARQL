package org.aksw.autosparql.search;

import org.apache.jena.rdf.model.Resource;

/**
 * @author Lorenz Buehmann
 */
public abstract class SearchResult {

	private final Resource resource;

	public SearchResult(Resource resource) {
		this.resource = resource;
	}

	/**
	 * @return the RDF resource
	 */
	public Resource getResource() {
		return resource;
	}
}
