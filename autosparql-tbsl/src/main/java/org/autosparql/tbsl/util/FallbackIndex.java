package org.autosparql.tbsl.util;

import java.util.List;

import org.autosparql.tbsl.model.BasicResultItem;

public interface FallbackIndex {

	List<BasicResultItem> getData(String queryString, int limit, int offset);
}
