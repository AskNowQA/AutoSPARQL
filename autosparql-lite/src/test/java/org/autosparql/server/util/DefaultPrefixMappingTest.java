package org.autosparql.server.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultPrefixMappingTest
{

	@Test
	public void test()
	{
		System.out.println(DefaultPrefixMapping.INSTANCE.expandPrefix("rdf:type"));
		//assertTrue(DefaultPrefixMapping.INSTANCE.expandPrefix("rdf:type"));
	}

}
