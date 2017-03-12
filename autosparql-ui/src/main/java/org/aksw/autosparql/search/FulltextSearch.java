package org.aksw.autosparql;

import org.apache.jena.rdf.model.Resource;

import java.util.Set;

/**
 * Fulltext search of RDF resources.
 *
 * @author Lorenz Buehmann
 */
public interface FulltextSearch {

	/**
	 * Returns a set of resources for the given search term.
	 *
	 * @param searchTerm the term used for the full text search
	 * @return a set of RDF resources
	 */
	Set<Resource> search(String searchTerm);
}
