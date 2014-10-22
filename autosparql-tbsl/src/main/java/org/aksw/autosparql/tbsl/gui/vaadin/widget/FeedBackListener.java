package org.aksw.autosparql.tbsl.gui.vaadin.widget;

import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;

public interface FeedBackListener {

	void positiveExampleSelected(BasicResultItem item);
	void negativeExampleSelected(BasicResultItem item);

}
