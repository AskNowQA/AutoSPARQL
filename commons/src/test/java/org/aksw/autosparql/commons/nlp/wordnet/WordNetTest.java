package org.aksw.autosparql.commons.nlp.wordnet;

import org.junit.Test;
import edu.mit.jwi.item.POS;

public class WordNetTest
{

	@Test public void testGetBestSynonyms()
	{
		System.out.println(new WordNet().getBestSynonyms(POS.VERB, "learn"));
	}

//	@Test public void testGetSisterTerms()
//	{
//		System.out.println(new WordNet().getSisterTerms(POS.NOUN, "actress"));
//	}

}