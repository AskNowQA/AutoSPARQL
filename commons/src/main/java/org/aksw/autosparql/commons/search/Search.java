package org.aksw.autosparql.commons.search;

import java.util.List;

public interface Search {
	List<String> getResources(String queryString);
	List<String> getResources(String queryString, int limit);

	int getTotalHits(String queryString);
	void setHitsPerPage(int hitsPerPage);

}