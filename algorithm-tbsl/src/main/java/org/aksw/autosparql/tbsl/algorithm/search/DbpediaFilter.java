package org.aksw.autosparql.tbsl.algorithm.search;

import org.dllearner.algorithms.qtl.filters.Filter;

public class DbpediaFilter implements Filter
{
	public static final DbpediaFilter INSTANCE = new DbpediaFilter();
	
	private DbpediaFilter() {}
	
	@Override public boolean isRelevantResource(String uri)
	{
		if(uri.startsWith("http://dbpedia.org/resource/Category:")) return false;
		return true;
	}

}