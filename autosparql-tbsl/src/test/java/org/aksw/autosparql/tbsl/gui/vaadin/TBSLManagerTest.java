package org.aksw.autosparql.tbsl.gui.vaadin;

import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.List;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;
import org.aksw.autosparql.tbsl.gui.vaadin.model.SelectAnswer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.QTL;
import org.junit.Test;

public class TBSLManagerTest
{

	@Test public void testTBSLManager()
	{
		Manager.getInstance().init();
		Logger.getLogger(QTL.class).setLevel(Level.DEBUG);
		TBSLManager man = new TBSLManager();
		// man.setKnowledgebase(man.getKnowledgebases().get(0));
		SelectAnswer a = (SelectAnswer) man.answerQuestion("Give me all houses with more than 3 bedrooms.");
		assertFalse(a.getItems().isEmpty());
		for(BasicResultItem item : a.getItems()) System.out.println(item.getLabel());

		List<String> p = new ArrayList<String>();
		p.add(a.getItems().get(1).getUri());
		p.add(a.getItems().get(2).getUri());
		p.add(a.getItems().get(0).getUri());
		List<String> n = new ArrayList<String>();
		man.refine(p, n);

	}
}