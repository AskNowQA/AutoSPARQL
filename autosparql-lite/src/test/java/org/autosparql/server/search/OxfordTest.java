package org.autosparql.server.search;

import static org.junit.Assert.assertTrue;
import java.util.SortedSet;
import org.autosparql.shared.Example;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class OxfordTest
{
	String[] queries =
		{
			"Give me flats in Oxford!",
			"Show me flats in Oxford with more than 50 square metres for less than 1000 pounds per month!",
			"flats with at least 2 bedrooms and a large kitchen",
			"Are there flats close to a kindergarten for less than 1000 pounds per month in Oxford?"
		};
	
	public void testOxford()
	{
		TBSLSearch search = TBSLSearch.getInstance(SparqlEndpoint.getEndpointDBpediaLiveAKSW(),"cache");
		SortedSet<Example> examples = search.getExamples(queries[0]);
		//System.out.println(examples);
		for(Example example : examples) System.out.println(example.getURI());
		//System.out.println(examples.get(0).getURI());
		// Example.equals() only uses the examples uri's
		System.out.println(examples);
		System.out.println(search.learnedQuery());
	}
}