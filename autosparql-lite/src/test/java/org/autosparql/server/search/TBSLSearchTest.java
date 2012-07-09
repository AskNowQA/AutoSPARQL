package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;
import java.net.MalformedURLException;
import java.util.SortedSet;
import org.autosparql.shared.Example;
import org.junit.Test;
import com.hp.hpl.jena.query.ARQ; 

public class TBSLSearchTest
{
	@Test
	public void testGetExamples() throws MalformedURLException
	{
		
		System.out.println("Creating TBSLSearch instance");
		TBSLSearch search = TBSLSearch.getDBpediaInstance();
		System.out.println("Creating AutoSPARQLSession instance");
//		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),	TBSLSearch.SOLR_DBPEDIA_RESOURCES, "cache");

		//List<Example> examples = search.getExamples("soccer clubs in Premier League");
		
		SortedSet<Example> examples = search.getExamples("Give me all books written by Dan Brown");
		//System.out.println(examples);
		for(Example example : examples) System.out.println(example.getURI());
		//System.out.println(examples.get(0).getURI());
		// Example.equals() only uses the examples uri's
		assertTrue(examples.toString()+'\n'+search.learnedQuery(),examples.contains(new Example("http://dbpedia.org/resource/Angels_&_Demons",null,null,null)));
		
//		session.fillExamples(examples);
//		for(Example example : examples){
//			System.out.println(example.get(RDFS.label.getURI()));
//		}
	}
}