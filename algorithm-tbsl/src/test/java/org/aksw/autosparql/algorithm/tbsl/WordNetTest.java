package org.aksw.autosparql.algorithm.tbsl;

import org.aksw.autosparql.commons.nlp.wordnet.WordNet;
import org.junit.Test;
import edu.mit.jwi.item.POS;

public class WordNetTest {

	@Test public void testWordNet()
	{
		WordNet wordNet = new WordNet();
		System.out.println(wordNet.getBestSynonyms(POS.VERB, "learn"));
	}	
//	/**
//	 * @param args
//	 * @throws JWNLException 
//	 */
//	public static void main(String[] args) throws JWNLException {
//		
//		WordNet wordnet = new WordNet();
//		
//		System.out.println(wordnet.getBestSynonyms(POS.NOUN,"mayor"));
//
//		PointerTargetNodeList relatedList;
//		for (Synset syn : wordnet.dict.getIndexWord(POS.NOUN,"mayor").getSenses()) {
//			relatedList = PointerUtils.getInstance().getSynonyms(syn);
//			Iterator<PointerTargetNode> i = relatedList.iterator();
//			while (i.hasNext()) {
//			  PointerTargetNode related = i.next();
//			  Synset s = related.getSynset();
//			  System.out.println("-- " + s);
//			}
//		}
//		
//	}
}