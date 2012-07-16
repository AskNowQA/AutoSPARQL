package org.autosparql.tbsl.widget;

import org.autosparql.tbsl.model.Answer;

public interface TBSLProgressListener {
	
	void message(String message);
	
	void foundAnswer(boolean answerFound);
	
	void finished(Answer answer);

}
