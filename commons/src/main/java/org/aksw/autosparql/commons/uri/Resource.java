/**
 *
 */
package org.aksw.autosparql.commons.uri;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.Constants;

/**
 * @author gerb
 *
 */
public class Resource {

	public double score;
	public String uri = Constants.NO_URI_FOUND;
	public Double aprioriScore = 0D;
	public List<String> context = new ArrayList<String>();
	public List<String> surfaceForms = new ArrayList<String>();
	public String comment = "";
	public String label = "";
	public String goldLabel = "";
	public String questionId = "";
	public List<String> types = new ArrayList<String>();
	public String dbpediaUri;

	public Resource(Integer id, String label) {

		this.questionId = id + "";
		this.goldLabel =  label;
	}

	public Resource() {
		// TODO Auto-generated constructor stub
	}

	public Resource(String uri) {this.uri=uri;}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return uri + " ("+questionId +", "+goldLabel+")";
	}

	@Override public boolean equals(Object o)
	{
		if(!(o instanceof Resource)) return false;
		return this.uri.equals(((Resource)o).uri);
	}

	@Override public int hashCode() {return uri.hashCode();}
}