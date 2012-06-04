package org.autosparql.server.search;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.SortedSet;
import org.autosparql.shared.Example;
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
		//System.out.println(examples);
		for(Example example : examples) System.out.println(example.getURI());
		//System.out.println(examples.get(0).getURI());
		// Example.equals() only uses the examples uri's
		System.out.println(examples);
		System.out.println(search.learnedQuery());
	}
}