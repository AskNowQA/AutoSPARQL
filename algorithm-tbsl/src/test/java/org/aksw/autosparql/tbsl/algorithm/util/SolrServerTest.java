package org.aksw.autosparql.tbsl.algorithm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;

public class SolrServerTest
{
		
	@Test public void testDBpediaIndices()
	{
		{
			List<String> resources = SolrServer.INSTANCE.resourcesIndex.getResources("Premier League");
			System.out.println(resources);
		}
		{
			List<String> datatypeProperties = SolrServer.INSTANCE.dataPropertiesIndex.getResources("old name");
			assertNotNull(datatypeProperties);
			assertFalse(datatypeProperties.isEmpty());			
			assertTrue(datatypeProperties.contains("http://dbpedia.org/ontology/oldName"));
			assertTrue(datatypeProperties.contains("http://dbpedia.org/ontology/formerName"));
		}
		{
			List<String> objectProperties = SolrServer.INSTANCE.objectPropertiesIndex.getResources("author");
			assertNotNull(objectProperties);
			assertFalse(objectProperties.isEmpty());
			assertTrue(objectProperties.contains("http://dbpedia.org/ontology/author"));
		}
		{
			List<String> objectProperties = SolrServer.INSTANCE.objectPropertiesIndex.getResources("written");
			assertNotNull(objectProperties);			
			assertFalse("boa doesn't find author by 'written'",objectProperties.isEmpty());
			assertTrue("boa doesn't find author by 'written'",objectProperties.contains("http://dbpedia.org/ontology/author"));
		}
		{
			List<String> resources = SolrServer.INSTANCE.resourcesIndex.getResources("Leipzig");
			assertNotNull(resources);
			assertFalse(resources.isEmpty());
			assertTrue(resources.contains("http://dbpedia.org/resource/Leipzig"));
		}
	}

}