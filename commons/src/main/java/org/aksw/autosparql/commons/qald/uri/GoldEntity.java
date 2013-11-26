package org.aksw.autosparql.commons.qald.uri;

import java.io.Serializable;

public class GoldEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6101450152774669016L;
	public String uri;
	public String label;

	public GoldEntity(String uri, String label) {
		
		this.uri = uri;
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GoldEntity other = (GoldEntity) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return uri;
	}
	
	
}
