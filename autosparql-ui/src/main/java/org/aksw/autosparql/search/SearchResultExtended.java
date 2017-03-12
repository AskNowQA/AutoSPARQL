package org.aksw.autosparql.search;

import org.apache.jena.rdf.model.Resource;

/**
 * An extended search result containing additional information (if exists), i.e.
 * <ul>
 *     <li>rdfs:label</li>
 *     <li>rdfs:comment</li>
 *     <li>rdf:type, i.e. the types of the resource</li>
 * </ul>
 *
 * @author Lorenz Buehmann
 */
public class SearchResultExtended extends SearchResult {

	private String label;
	private String comment;
	private String types;

	public SearchResultExtended(Resource resource, String label, String comment, String types) {
		super(resource);
		this.label = label;
		this.comment = comment;
		this.types = types;
	}


	/**
	 * Gets the RDFS label
	 *
	 * @return the RDFS label
	 */
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the RDFS comment
	 *
	 * @return the RDFS comment
	 */
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Gets the types
	 *
	 * @return the types
	 */
	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SearchResult)) return false;

		SearchResult that = (SearchResult) o;

		return getResource().equals(that.getResource());
	}

	@Override
	public int hashCode() {
		return getResource().hashCode();
	}

}
