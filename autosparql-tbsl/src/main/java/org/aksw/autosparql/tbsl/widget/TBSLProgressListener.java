package org.aksw.autosparql.tbsl.widget;

import org.aksw.autosparql.tbsl.model.Answer;

public interface TBSLProgressListener {
	
	void message(String message);
	
	void foundAnswer(boolean answerFound);
	
	void finished(Answer answer);

}
