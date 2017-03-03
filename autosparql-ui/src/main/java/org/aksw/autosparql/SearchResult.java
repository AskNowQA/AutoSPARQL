package org.aksw.autosparql;

import org.apache.jena.rdf.model.Resource;

public class SearchResult {

	private Resource resource;
	private String label;
	private String comment;
	private String types;

	public SearchResult(Resource resource, String label, String comment, String types) {
		this.resource = resource;
		this.label = label;
		this.comment = comment;
		this.types = types;
	}

	/**
	 * Gets resource
	 *
	 * @return value of resource
	 */
	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	/**
	 * Gets label
	 *
	 * @return value of label
	 */
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets comment
	 *
	 * @return value of comment
	 */
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Gets types
	 *
	 * @return value of types
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

		return resource.equals(that.resource);
	}

	@Override
	public int hashCode() {
		return resource.hashCode();
	}
}