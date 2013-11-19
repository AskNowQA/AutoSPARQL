package org.aksw.autosparql.tbsl.util;

import java.util.List;
import org.aksw.autosparql.tbsl.model.BasicResultItem;

public interface FallbackIndex {

	List<BasicResultItem> getData(String queryString, int limit, int offset);
}
