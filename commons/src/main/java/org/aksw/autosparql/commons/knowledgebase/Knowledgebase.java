package org.aksw.autosparql.commons.knowledgebase;

import org.aksw.rdfindex.Indices;
import com.hp.hpl.jena.query.ResultSet;

public abstract class Knowledgebase
{	
	private final String label;
	private final String description;
	protected final Indices indices;
	
	public abstract ResultSet querySelect(String query);

	public Knowledgebase(String label, String description, Indices indices)
	{
		this.label = label;
		this.description = description;
		this.indices = indices;
	}

	public String getLabel() {return label;}
	public String getDescription() {return description;}
	public Indices getIndices() {return indices;}	
	@Override public String toString() {return label;}
}