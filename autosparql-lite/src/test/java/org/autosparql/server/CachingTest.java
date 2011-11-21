package org.autosparql.server;
import static org.junit.Assert.assertTrue;
import static org.autosparql.server.AutoSPARQLSession.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.autosparql.shared.Example;
import org.junit.Before;
import org.junit.Test;

public class CachingTest
{
	Example e1 = new Example("http://dbpedia.org/resource/Berlin");
	Example e2 = new Example("http://dbpedia.org/resource/Leipzig");
	SortedSet<Example> examples;
	

	@Before
	public void before()
	{
		e1.set("rdfs:label", "Berlin");
		e2.set("rdfs:label", "Leipzig");
		examples = new TreeSet<Example>(Arrays.asList(new Example[]{e1,e2}));
	}

	@Test
	public void testExampleToMapToExample()
	{
		//System.out.println(AutoSPARQLSession.examplesToMaps(examples));
		SortedSet<Example> converted = AutoSPARQLSession.mapsToExamples(AutoSPARQLSession.examplesToMaps(examples));
		//System.out.println(converted);
		// it's sorted, so Berlin should be first
		assertTrue(e1.deepEquals(converted.first()));
		assertTrue(e2.deepEquals(converted.last()));
	}

	@Test
	public void ehCacheTest()
	{
		String[] array = new String[]{"test","best","schmest"};
		{
		Cache cache = getCacheManager().getCache("test");
		cache.put(new Element("array",array));
		cache.flush();
		}
		Cache cache2 = getCacheManager().getCache("test");
		assertTrue(Arrays.equals((String[])cache2.get("array").getValue(),array));
	}
		
	@Test
	public void cacheAndRetrieveExamples()
	{
		{
		Cache cache = CacheManager.getInstance().getCache("test");
		List<Map<String,Object>> list = AutoSPARQLSession.examplesToMaps(examples);
		cache.put(new Element("testquery",list));
		}
		Cache cache2 = getCacheManager().getCache("test");
		SortedSet<Example> retrieved = AutoSPARQLSession.mapsToExamples((List<Map<String,Object>>)cache2.get("testquery").getValue());
		assertTrue(e1.deepEquals(retrieved.first()));
		assertTrue(e2.deepEquals(retrieved.last()));
	}
	
//	//@Test
//	public void testSave()
//	{
//		Cache cache = CacheManager.getInstance().getCache("test");
//		cache.put(new Element("test","testwert"));
//		getCacheManager().shutdown();
//	}
//	
//	@Test
//	public void testLoad()
//	{
//		Cache cache = CacheManager.getInstance().getCache("test");
//		System.out.println(cache.get("test").getValue());
//		getCacheManager().shutdown();
//	}
}