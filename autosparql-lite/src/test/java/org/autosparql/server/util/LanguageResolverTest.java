package org.autosparql.server.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class LanguageResolverTest
{
	@Test
	public void test()
	{
		LanguageResolver r = new LanguageResolver();
		assertTrue(r.resolve("Digital Fortress@en", "Цифровая крепость@ru").equals("Digital Fortress@en"));
		assertTrue(r.resolve("Цифровая крепость@ru", "Digital Fortress@en").equals("Digital Fortress@en"));
	}
}