package org.aksw.autosparql.tbsl.gui.vaadin.util;

import java.util.List;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;

public interface FallbackIndex {

	List<BasicResultItem> getData(String queryString, int limit, int offset);
}
