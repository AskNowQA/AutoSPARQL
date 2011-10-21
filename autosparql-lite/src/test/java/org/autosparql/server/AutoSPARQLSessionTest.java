package org.autosparql.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class AutoSPARQLSessionTest
{
	static final AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources");
	
	//@Test
	public void testGetExamples()
	{
		System.out.println(session.getExamples("European Union countries"));
	}
	
	@Test
	public void testGetExamplesByQTL()
	{
//		String[] positives = {"http://dbpedia.org/resource/Angels_&_Demons","http://dbpedia.org/resource/Digital_Fortress"};
//		String[] negatives = {"http://dbpedia.org/resource/The_Baby_in_the_Manger"};
		//"http://dbpedia.org/resource/Meet_Joe_Black"
		String[] positives = {"http://dbpedia.org/resource/The_Mexican","http://dbpedia.org/resource/Meet_Joe_Black"};
//		String[] negatives = {"http://dbpedia.org/resource/Lara_Breay"};
		String[] negatives = {"http://dbpedia.org/resource/The_Lion_King"};
		
		Set<Example> examples = new HashSet<Example>(session.getExamplesByQTL(Arrays.asList(positives), Arrays.asList(negatives),new HashSet<String>(Arrays.asList(new String[] {"film","starring","Brad Pitt"}))));
		//for(Example example: examples) System.out.println(example.getURI());
		System.out.println(examples);
	}

}