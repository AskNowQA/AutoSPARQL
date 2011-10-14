package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.List;

import org.autosparql.shared.Example;
import org.junit.Test;

public class SolrSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		SolrSearch search = new SolrSearch("http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources");
		List<Example> examples = search.getExamples("books written by Dan Brown");
		assertTrue(examples.contains(new Example("http://dbpedia.org/resource/Digital_Fortress",null,null,null)));
	}		
}