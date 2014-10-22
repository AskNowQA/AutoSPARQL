package org.aksw.autosparql.commons.search;

import org.aksw.rdfindex.Index;
import org.aksw.rdfindex.IndexItem;
import org.aksw.rdfindex.IndexResultSet;
/*import javax.annotation.Nonnull;*/
import org.dllearner.algorithms.qtl.filters.Filter;

public class FilteredIndex extends Index
{
	/*@Nonnull*/ final Index index;
	/*@Nonnull*/ final Filter filter;

	public FilteredIndex(/*@Nonnull*/ Index index,/*@Nonnull*/  Filter filter) {this.index=index;this.filter=filter;}

	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit)
	{
		IndexResultSet result = index.getResourcesWithScores(queryString, limit);
		IndexResultSet filtered = new IndexResultSet();
		for(IndexItem item : result) {if(filter.isRelevantResource(item.getUri())) filtered.add(item);}
		return filtered;
	}

}