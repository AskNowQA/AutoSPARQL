package org.autosparql.server;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class AutoSPARQLSessionTest
{
	@Test
	public void testGetExamples()
	{
		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources");
		System.out.println(session.getExamples("European Union countries"));
	}

}
