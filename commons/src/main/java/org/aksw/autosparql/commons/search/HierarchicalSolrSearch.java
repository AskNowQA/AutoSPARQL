package org.aksw.autosparql.commons.search;

public class HierarchicalSolrSearch extends SolrSearch {
	
	private SolrSearch primarySearch;
	private SolrSearch secondarySearch;
	
	public HierarchicalSolrSearch(SolrSearch primarySearch, SolrSearch secondarySearch) {
		this.primarySearch = primarySearch;
		this.secondarySearch = secondarySearch;
	}
	
	@Override
	public SolrQueryResultSet getResourcesWithScores(String queryString, int limit, int offset, boolean sorted) {
		SolrQueryResultSet rs = primarySearch.getResourcesWithScores(queryString, limit, offset, sorted);
		if(rs.getItems().size() < limit){
			rs.add(secondarySearch.getResourcesWithScores(queryString, limit-rs.getItems().size(), offset, sorted));
		}
		return rs;
	}

}
