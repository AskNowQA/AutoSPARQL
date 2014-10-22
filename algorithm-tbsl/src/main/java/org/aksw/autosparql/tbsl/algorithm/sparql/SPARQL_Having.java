package org.aksw.autosparql.tbsl.algorithm.sparql;

public class SPARQL_Having implements Cloneable{

	public String filter;

	@Override public SPARQL_Having clone()
	{
		return new SPARQL_Having(filter);
	}

	public SPARQL_Having(String s) {
		filter = s;
	}

	public String toString() {
		return "HAVING (" + filter + ")";
	}
}
