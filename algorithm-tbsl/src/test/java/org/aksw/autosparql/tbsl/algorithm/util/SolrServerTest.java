package org.aksw.autosparql.tbsl.algorithm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.aksw.autosparql.commons.index.SolrServer;
import org.junit.Test;

public class SolrServerTest
{

	@Test public void testDBpediaIndices()
	{
		{
			List<String> resources = SolrServer.INSTANCE.resourcesIndex.getResources("Premier League");
//			System.out.println(resources);
		}
		// can't find entry with data property, old name doesn't work
//		{
//			List<String> datatypeProperties = SolrServer.INSTANCE.dataPropertiesIndex.getResources("old name");
//			assertNotNull(datatypeProperties);
//			assertFalse(datatypeProperties.isEmpty());
////			System.out.println(datatypeProperties);
//			assertTrue(datatypeProperties.contains("http://dbpedia.org/ontology/oldName"));
////			assertTrue(datatypeProperties.contains("http://dbpedia.org/ontology/formerName"));
//		}
		{
			List<String> objectProperties = SolrServer.INSTANCE.objectPropertiesIndex.getResources("author");
			assertNotNull(objectProperties);
			assertFalse(objectProperties.isEmpty());
			assertTrue(objectProperties.contains("http://dbpedia.org/ontology/author"));
		}
		{
			List<String> objectProperties = SolrServer.INSTANCE.objectPropertiesIndex.getResources("married");
			assertNotNull(objectProperties);
			assertFalse(objectProperties.isEmpty());
			assertTrue(objectProperties.contains("http://dbpedia.org/ontology/spouse"));
		}
		{
			List<String> objectProperties = SolrServer.INSTANCE.dbpediaIndices.getObjectPropertyIndex().getResources("written");
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