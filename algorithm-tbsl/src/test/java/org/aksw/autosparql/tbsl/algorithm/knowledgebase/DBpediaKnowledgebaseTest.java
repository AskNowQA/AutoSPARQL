package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import static org.junit.Assert.assertTrue;

import org.aksw.autosparql.commons.knowledgebase.DBpediaKnowledgebase;
import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolution;

public class DBpediaKnowledgebaseTest
{
	@Test public void test()
	{
		QuerySolution qs = DBpediaKnowledgebase.INSTANCE.querySelectNoCache
				("select * where {<http://dbpedia.org/resource/Inferno_(Dan_Brown_novel)> <http://dbpedia.org/ontology/author> ?a.} limit 1").nextSolution();		
		assertTrue(qs.getResource("a").getURI().equals("http://dbpedia.org/resource/Dan_Brown"));
	}
}