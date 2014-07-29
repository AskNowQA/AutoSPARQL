package org.aksw.autosparql.commons.nlp.pos;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StanfordPartOfSpeechTaggerTest
{

	@Test public void testStanfordPartOfSpeechTagger()
	{
		String s = StanfordPartOfSpeechTagger.INSTANCE.tag("The quick brown fox jumps over the lazy dog.");
		assertEquals("The/DT quick/JJ brown/JJ fox/NN jumps/VBZ over/IN the/DT lazy/JJ dog/NN ./.",s);
	}

}