package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.SortedSet;

import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class TBSLSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		System.out.println("Creating TBSLSearch instance");
		TBSLSearch search = new TBSLSearch(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),"cache");
		System.out.println("Creating AutoSPARQLSession instance");
//		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),	TBSLSearch.SOLR_DBPEDIA_RESOURCES, "cache");

		//List<Example> examples = search.getExamples("soccer clubs in Premier League");
		
		SortedSet<Example> examples = search.getExamples("Give me all books written by Dan Brown");
		//System.out.println(examples);
		for(Example example : examples) System.out.println(example.getURI());
		//System.out.println(examples.get(0).getURI());
		// Example.equals() only uses the examples uri's
		System.out.println(examples);
		assertTrue(examples.contains(new Example("http://dbpedia.org/resource/The_Da_Vinci_Code",null,null,null)));
		System.out.println(search.learnedQuery());
		

//		session.fillExamples(examples);
//		for(Example example : examples){
//			System.out.println(example.get(RDFS.label.getURI()));
//		}
	}
}