package org.aksw.autosparql.tbsl.algorithm.search;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.dllearner.algorithms.qtl.filters.Filter;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.IndexResultItem;
import org.dllearner.common.index.IndexResultSet;

public class FilteredIndex extends Index
{
	@Nonnull final Index index;
	@Nonnull final Filter filter; 
	
	public FilteredIndex(@Nonnull Index index,@Nonnull  Filter filter) {this.index=index;this.filter=filter;}
		
	@Override public List<String> getResources(String queryString, int limit, int offset)
	{
		List<String> resources = index.getResources(queryString, limit,offset);
		// in java 8: resources.stream().filter(r -> filter.isRelevantResource(r)).collect(); ?
		List<String> filtered = new ArrayList<String>();
		for(String r: resources) {if(filter.isRelevantResource(r)) filtered.add(r);}
		return filtered;
	}

	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit, int offset)
	{
		IndexResultSet result = index.getResourcesWithScores(queryString, limit,offset);
		IndexResultSet filtered = new IndexResultSet();
		for(IndexResultItem item : result.getItems()) {if(filter.isRelevantResource(item.getUri())) filtered.addItem(item);}
		return filtered;
	}

}