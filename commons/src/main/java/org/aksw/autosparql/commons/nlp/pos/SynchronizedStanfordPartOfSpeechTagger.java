package org.aksw.autosparql.commons.nlp.pos;

public class SynchronizedStanfordPartOfSpeechTagger extends StanfordPartOfSpeechTagger
{
	@Override public synchronized String tag(String sentence) {return super.tag(sentence);}
}