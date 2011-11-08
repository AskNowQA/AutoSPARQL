package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.SortedSet;

import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class TBSLSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		TBSLSearch search = new TBSLSearch(new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql")),"cache");
		//List<Example> examples = search.getExamples("soccer clubs in Premier League");
		SortedSet<Example> examples = search.getExamples("books written by Dan Brown");
		//System.out.println(examples);
		for(Example example : examples) System.out.println(example.getURI());
//System.out.println(examples.get(0).getURI());
		// Example.equals() only uses the examples uri's
		assertTrue(examples.contains(new Example("http://dbpedia.org/resource/The_Da_Vinci_Code",null,null,null)));
	}
}