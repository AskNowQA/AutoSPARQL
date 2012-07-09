//package org.autosparql.server.util;
//
//import static org.junit.Assert.*;
//
//import org.dllearner.kb.sparql.SparqlEndpoint;
//import org.dllearner.kb.sparql.SparqlQuery;
//import org.junit.Test;
//
//public class NoCacheTest {
//
//	@Test
//	public void testExecuteSelectQuery(){
//		String json = new NoCache().executeSelectQuery(SparqlEndpoint.getEndpointDBpedia(), "select ?label where {<http://dbpedia.org/resource/Leipzig> rdfs:label ?label}");
//		System.out.println(SparqlQuery.convertJSONtoResultSet(json).next());
//	}
//
//}
