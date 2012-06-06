package org.autosparql.server.util;

import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class NoCache
{
	public String executeSelectQuery(SparqlEndpoint endpoint, String query)
	{
		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		String json = SparqlQuery.convertResultSetToJSON(queryExecution.execSelect());
		return json;
	}
}
