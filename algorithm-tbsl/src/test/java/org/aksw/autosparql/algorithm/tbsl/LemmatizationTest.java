package org.aksw.autosparql.algorithm.tbsl;

import org.aksw.autosparql.commons.nlp.lemma.LingPipeLemmatizer;

public class LemmatizationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LingPipeLemmatizer lem = new LingPipeLemmatizer();

		System.out.println(lem.stem("soccer clubs"));
		System.out.println(lem.stem("Permier League","NNP"));
		System.out.println(lem.stem("cities","NNS"));
		System.out.println(lem.stem("killed"));
		System.out.println(lem.stem("bigger"));
	}

}
