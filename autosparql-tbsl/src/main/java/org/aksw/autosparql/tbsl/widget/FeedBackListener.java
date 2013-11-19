package org.aksw.autosparql.tbsl.widget;

import org.aksw.autosparql.tbsl.model.BasicResultItem;

public interface FeedBackListener {
	
	void positiveExampleSelected(BasicResultItem item);
	void negativeExampleSelected(BasicResultItem item);

}
