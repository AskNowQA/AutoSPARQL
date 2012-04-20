package org.autosparql.server;

import org.junit.Test;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class DependencyTest
{
	@Test
	public void testARQ()
	{
		ARQ a = new ARQ();		
	}
	
//	@Test
//	public void queryTest()
//	{
//		QueryEngineHTTP qe = new QueryEngineHTTP("http://dbpedia.org/sparql", "select * {?s rdfs:label ?o.} limit 10");
//		ResultSet rs = qe.execSelect();
//		while(rs.hasNext())
//		{
//			QuerySolution qs = rs.next();
//			System.out.println(qs.get("s"));
//		}
//	}
	
}