package org.aksw.autosparql.commons.uri;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UriDisambiguationTest
{

	@Test public void testGetUriCandidates()
	{
		assertTrue(UriDisambiguation.getUriCandidates("New Jersey", "en").contains(new Resource("http://dbpedia.org/resource/New_Jersey")));		
	}


	@Test public void testGetUriListOfResourceStringString()
	{
		assertTrue(UriDisambiguation.getTopUris(UriDisambiguation.getUriCandidates("New Jersey", "en"), "New Jerysey", "en").get(0).equals(new Resource("http://dbpedia.org/resource/New_Jersey")));	
	}

	@Test public void testGetUriQuestionEntityString()
	{
	}

}