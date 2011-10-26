package org.autosparql.client;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TransformerTest
{
	@Test
	public void testDisplayProperty()
	{
		assertTrue(Transformer.displayProperty("http://dbpedia.org/Hardcover").equals("Hardcover"));
	}

	@Test
	public void testDisplayObject()
	{
		assertTrue(Transformer.displayObject("http://dbpedia.org/Hardcover").equals("Hardcover"));
		assertTrue(Transformer.displayObject("First edition cover").equals("First edition cover"));
	}
}