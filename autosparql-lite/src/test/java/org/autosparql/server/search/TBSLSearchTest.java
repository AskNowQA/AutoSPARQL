package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class TBSLSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		TBSLSearch search = new TBSLSearch(new SparqlEndpoint(new URL("http://dbpedia.org/sparql")));
		List<Example> examples = search.getExamples("soccer clubs in Premier League");
		//List<Example> examples = search.getExamples("books written by Dan Brown");
		System.out.println(examples);
		for(String x : examples.get(0).getPropertyNames()) System.out.println(x);

		
		assertTrue(examples.contains(new Example("http://dbpedia.org/resource/Arsenal_F.C.",null,null,null)));
	}
}