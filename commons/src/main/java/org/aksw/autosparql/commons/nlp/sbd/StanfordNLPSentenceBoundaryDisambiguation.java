package org.aksw.autosparql.commons.nlp.sbd;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

/**
 *
 * @author Daniel Gerber
 */
public final class StanfordNLPSentenceBoundaryDisambiguation {

    /**
     *
     * @param text
     * @return
     */
	public static List<String> getSentences(String text) {

		List<String> sentences = new ArrayList<String>();

		Reader stringReader = new StringReader(text);
		DocumentPreprocessor preprocessor = new DocumentPreprocessor(stringReader,  DocumentPreprocessor.DocType.Plain);

		Iterator<List<HasWord>> iter = preprocessor.iterator();
		while ( iter.hasNext() ) {

			List<String> sentence = new ArrayList<String>();
			for ( HasWord word : iter.next() ) sentence.add(word.toString());

			sentences.add(StringUtils.join(sentence, " "));
		}

		return sentences;
	}
}
