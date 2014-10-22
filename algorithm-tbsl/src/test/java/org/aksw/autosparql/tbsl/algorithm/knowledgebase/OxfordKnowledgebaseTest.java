package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import static org.junit.Assert.assertTrue;

import org.aksw.autosparql.commons.knowledgebase.OxfordKnowledgebase;
import org.junit.Test;

public class OxfordKnowledgebaseTest
{
	/**  can be used to benchmark the model loading time*/
	@Test public void testModelNotEmpty()
	{
		assertTrue(OxfordKnowledgebase.INSTANCE.getModel().size()>0);
	}

	@Test public void testQuerySelect()
	{
		assertTrue(OxfordKnowledgebase.INSTANCE.querySelect("select * where {?s a <http://diadem.cs.ox.ac.uk/ontologies/real-estate#House>.} limit 1")
				.nextSolution().getResource("s").getURI().startsWith("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
	}

}