package org.aksw.autosparql.commons.uri;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class UriDisambiguationTest
{

	@Test public void testGetUriCandidates()
	{
		System.out.println(UriDisambiguation.getUriCandidates("Leipzig", "en"));
//		assertTrue(UriDisambiguation.getUriCandidates("Leipzig", "en").contains(new Resource("http://dbpedia.org/resource/Leipzig")));		
	}


	@Test public void testGetUriListOfResourceStringString()
	{
		assertTrue(UriDisambiguation.getTopUris(UriDisambiguation.getUriCandidates("Leipzig", "en"), "Leipzig", "en").get(0).equals(new Resource("http://dbpedia.org/resource/Leipzig")));	
	}

	@Test public void testGetUriQuestionEntityString()
	{
	}

}