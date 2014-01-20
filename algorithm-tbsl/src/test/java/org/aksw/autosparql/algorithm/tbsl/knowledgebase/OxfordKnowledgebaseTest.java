package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import static org.junit.Assert.*;
import org.junit.Test;

public class OxfordKnowledgebaseTest
{
	@Test public void test()
	{
		assertTrue(OxfordKnowledgebase.INSTANCE.querySelect("select * where {?s a <http://diadem.cs.ox.ac.uk/ontologies/real-estate#House>.} limit 1")
				.nextSolution().getResource("s").getURI().startsWith("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
	}

}