package org.aksw.autosparql.algorithm.tbsl.search;

import static org.junit.Assert.*;
import java.util.List;
import org.junit.Test;

public class SolrSearchTest
{
	String SERVER_URI	= "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_resources";

	@Test public void testGetResourcesWithScoresString()
	{
		List<String> resources = new SolrSearch(SERVER_URI,"label").getResources("Leipzig");
		assertNotNull(resources);
		assertFalse(resources.isEmpty());
		assertTrue(resources.contains("http://dbpedia.org/resource/Leipzig"));
	}

}