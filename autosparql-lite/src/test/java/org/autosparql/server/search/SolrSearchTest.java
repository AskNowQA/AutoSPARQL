package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.SortedSet;

import org.autosparql.shared.Example;
import org.junit.Test;

public class SolrSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		SolrSearch search = new SolrSearch();
		SortedSet<Example> examples = search.getExamples("books written by Dan Brown");
		System.out.println(examples);
		assertTrue(examples.contains(new Example("http://dbpedia.org/resource/Digital_Fortress",null,null,null)));
	}		
}