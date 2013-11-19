package org.autosparql.server.search;

import static org.junit.Assert.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.SortedSet;
import org.aksw.autosparql.server.search.TBSLSearch;
import org.aksw.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class OxfordTest
{
	String[] queries =
		{
			"Give me houses in Oxford!",
			"Show me flats in Oxford with more than 50 square metres for less than 1000 pounds per month!",
			"Give me all houses with more than 2 bedrooms.",
			"Are there flats close to a kindergarten for less than 1000 pounds per month in Oxford?"
		};
	String query = queries[2];
	
	@Test
	public void testOxford() throws MalformedURLException
	{		
		TBSLSearch search = TBSLSearch.getOxfordInstance();
		
		SortedSet<Example> examples = search.getExamples(query);
		assertTrue(examples.iterator().next().get("uri").toString()
				.startsWith("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
		
//		for(Example example : examples) System.out.println(example.getURI());
//
//		System.out.println(examples);
		//System.out.println(search.learnedQuery());
	}
}