package org.autosparql.server;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.autosparql.shared.Example;
import org.junit.Test;

public class AutoSPARQLSessionTest
{
	//	final AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),
	//			"http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources",
	//			"cache");
	final AutoSPARQLSession session = new AutoSPARQLServiceImpl().getAutoSPARQLSession();

	//@Test
	public void testFillExamples()
	{
		SortedSet<Example> examples = new TreeSet<Example>(Arrays.asList(new Example[] {new Example("http://dbpedia.org/resource/Leipzig")}));
		session.fillExamples(examples);
		System.out.println(examples);
		assertTrue(examples.iterator().next().get("http://dbpedia.org/ontology/postalCode").equals("04001-04357@en"));
		//System.out.println(examples.getFirst());
	}

	@Test
	public void testGetExamples()
	{
		//System.out.println(session.getExamples("European Union countries"));
		session.setFastSearch(true);
		SortedSet<Example> examples = session.getExamples("Books written by Dan Brown");				
		Example example = new Example("http://dbpedia.org/resource/Digital_Fortress");
		assertTrue("Example "+example+" missing in results. Found values: "+'\n'+
				Example.getURIs(examples).toString().replace(',', '\n'),examples.contains(example));			
		//for(Example example: examples) System.out.println(example.getURI());
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
		Example example = new Example("http://dbpedia.org/resource/Inglourious_Basterds");
		assertTrue("Example "+example+" missing in results. Found values: "+'\n'+
				Example.getURIs(examples).toString().replace(',', '\n'),examples.contains(example));				

		positives = new String[] {"http://dbpedia.org/resource/Angels_&_Demons"};
		negatives = new String[] {"http://dbpedia.org/resource/Arthur%27s_Teacher_Trouble"};
		examples = new HashSet<Example>(session.getExamplesByQTL(Arrays.asList(positives), Arrays.asList(negatives),new HashSet<String>(Arrays.asList(new String[] {"film","starring","Brad Pitt"}))));

	}
}