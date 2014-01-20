package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.hp.hpl.jena.query.QuerySolution;

public class DBpediaKnowledgebaseTest
{
	@Test public void test()
	{
		QuerySolution qs = DBpediaKnowledgebase.INSTANCE.querySelect
				("select * where {<http://dbpedia.org/resource/Inferno_(Dan_Brown_novel)> <http://dbpedia.org/property/author> ?a.} limit 1").nextSolution();
		assertTrue(qs.getResource("a").getURI().equals("http://dbpedia.org/resource/Dan_Brown"));
	}
}