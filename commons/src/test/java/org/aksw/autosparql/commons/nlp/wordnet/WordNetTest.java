package org.aksw.autosparql.commons.nlp.wordnet;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.mit.jwi.item.POS;

public class WordNetTest
{

	@Test public void testGetBestSynonyms()
	{
		assertTrue(new WordNet().getBestSynonyms(POS.VERB, "learn").contains("acquire"));
	}

//	@Test public void testGetSisterTerms()
//	{
//		System.out.println(new WordNet().getSisterTerms(POS.NOUN, "actress"));
//	}

}