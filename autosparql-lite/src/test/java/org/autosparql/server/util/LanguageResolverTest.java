package org.autosparql.server.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LanguageResolverTest
{
	@Test
	public void test()
	{
		LanguageResolver r = new LanguageResolver();
		assertTrue(r.resolve("Digital Fortress@en", "Цифровая крепость@ru").equals("Digital Fortress@en"));
		assertTrue(r.resolve("Цифровая крепость@ru", "Digital Fortress@en").equals("Digital Fortress@en"));
	}
	
//	@Test
//	public void test2() throws Exception {
//		
//		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpedia(), 
//				"http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources", 
//				"cache");
//
////		final AutoSPARQLSession session;
////		session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),
////				"http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources",
////				"cache");
//		session.setFastSearch(true);
//		List<String> uris = Arrays.asList(new String[]{
//				"http://dbpedia.org/resource/Angels_&_Demons",
//				"http://dbpedia.org/resource/Deception_Point",
//				"http://dbpedia.org/resource/Digital_Fortress",
//				"http://dbpedia.org/resource/The_Da_Vinci_Code"});
//		Set<Example> examples = session.getExamples("books written by Dan Brown");
//		
//		System.out.println(examples.iterator().next().get(RDFS.label.getURI()));
//
//	}
}