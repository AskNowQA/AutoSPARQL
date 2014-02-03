package org.aksw.autosparql.tbsl.algorithm.learning;

public class Entity {

	private String uri;
	private String label;
	
	public Entity(String uri, String label) {
		this.uri = uri;
		this.label = label;
	}
	
	public Entity(String uri) {
		this(uri, null);
	}
	
	public String getURI() {
		return uri;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return uri + "(" + label + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	

}
