package org.aksw.autosparql.commons.index;

import java.util.List;
import org.dllearner.common.index.IndexResultSet;
import org.dllearner.common.index.SPARQLIndex;

/** Index decorator that uses tries stemming on the input in case of failure to find.**/
public class StemmedIndex extends SPARQLIndex
{
	protected SPARQLIndex index;	
	
	public StemmedIndex(SPARQLIndex index) {super(index);this.index=index;}

	@Override public List<String> getResources(String arg0)
	{
		return null;
	}

	@Override public List<String> getResources(String arg0, int arg1)
	{
		return null;
	}

	@Override public List<String> getResources(String arg0, int arg1, int arg2)
	{
		return null;
	}

	@Override public IndexResultSet getResourcesWithScores(String arg0)
	{
		return null;
	}

	@Override public IndexResultSet getResourcesWithScores(String arg0, int arg1)
	{
		return null;
	}

	@Override public IndexResultSet getResourcesWithScores(String arg0, int arg1, int arg2)
	{
		return null;
	}

}