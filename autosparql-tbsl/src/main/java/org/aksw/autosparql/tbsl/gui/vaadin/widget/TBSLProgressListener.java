package org.aksw.autosparql.tbsl.gui.vaadin.widget;

import org.aksw.autosparql.tbsl.gui.vaadin.model.Answer;

public interface TBSLProgressListener {
	
	void message(String message);
	
	void foundAnswer(boolean answerFound);
	
	void finished(Answer answer);

}
