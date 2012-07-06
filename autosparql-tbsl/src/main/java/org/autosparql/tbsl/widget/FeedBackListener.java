package org.autosparql.tbsl.widget;

import org.autosparql.tbsl.model.BasicResultItem;

public interface FeedBackListener {
	
	void positiveExampleSelected(BasicResultItem item);
	void negativeExampleSelected(BasicResultItem item);

}
