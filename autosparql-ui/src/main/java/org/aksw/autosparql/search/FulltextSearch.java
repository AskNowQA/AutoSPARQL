package org.aksw.autosparql.search;

import org.apache.jena.rdf.model.Resource;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Fulltext search of RDF resources.
 *
 * @author Lorenz Buehmann
 */
public interface FulltextSearch<T extends SearchResult> {

	/**
	 * Returns the result of a fulltext search for the given search term.
	 *
	 * @param searchTerm the term used for the full text search
	 * @return a stream of search results
	 */
	Stream<T> search(String searchTerm);
}
