/**
 * 
 */
package org.aksw.autosparql.commons.qald.uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.uri.Resource;

/**
 * @author gerb
 *
 */
public class Entity implements Serializable {

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		Entity other = (Entity) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7859357081713774767L;
	
	public String label = "";
	public String type = "";
	public List<Resource> uris = new ArrayList<Resource>();

	/**
	 * 
	 * @param label
	 * @param type
	 */
	public Entity(String label, String type) {
		
		this.label = label;
		this.type = type;
	}
	
	public String toString(){
		
		return label + "("+type+")";
	}

}
