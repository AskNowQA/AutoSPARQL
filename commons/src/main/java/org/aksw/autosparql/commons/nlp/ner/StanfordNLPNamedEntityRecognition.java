/**
 * 
 */
package org.aksw.autosparql.commons.nlp.ner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.aksw.autosparql.commons.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.apache.commons.lang3.StringUtils;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreLabel;


/**
 * @author gerb
 *
 */
public class StanfordNLPNamedEntityRecognition {

	private static final String NAMED_ENTITY_TAG_DELIMITER = "_";
	
	private CRFClassifier<CoreLabel> classifier;
	private StanfordNLPSentenceBoundaryDisambiguation sbd   = new StanfordNLPSentenceBoundaryDisambiguation();

	/**
	 * 
	 */
	public StanfordNLPNamedEntityRecognition(String classifierPath) {
		
		try {

            // this is used to surpress the "error" messages from stanford etc.
//		    PrintStream standardErrorStream = System.err;
//            System.setErr(new PrintStream(new ByteArrayOutputStream()));

            this.classifier = CRFClassifier.getClassifier(classifierPath);
            
            // revert to original standard error stream
//            System.setErr(standardErrorStream);
		}
		catch (ClassCastException e) {
			
			e.printStackTrace();
			throw new RuntimeException("Wrong classifier specified in config file.", e);
		}
		catch (IOException e) {

			e.printStackTrace();
			throw new RuntimeException("Could not read trained model!", e);
		}
		catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			throw new RuntimeException("Wrong classifier specified in config file.", e);
		} 
	}

	/**
	 * 
	 * @param string
	 * @return
	 */
	public String getAnnotatedSentence(String sentenceString) {

		List<String> sentenceTokens = new ArrayList<String>();
		
		for ( List<CoreLabel> sentence : ((List<List<CoreLabel>>) classifier.classify(sentenceString)) ) {
		
			for ( CoreLabel word : sentence ) {
				
				String normalizedTag = NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(word.get(AnswerAnnotation.class));
				if ( normalizedTag == null ) System.out.println(word);
				sentenceTokens.add(word.word() + NAMED_ENTITY_TAG_DELIMITER + normalizedTag);
			}
		}
		return StringUtils.join(sentenceTokens, " ");
	}
	
	/**
	 * 
	 * @param sentences
	 * @return
	 */
	public String getAnnotatedSentences(String sentences) {
	    
	    StringBuffer buffer = new StringBuffer();
	    for ( String sentence : sbd.getSentences(sentences) ) 
	        buffer.append(getAnnotatedSentence(sentence)).append("\n");
	            
	    return buffer.toString();
	}

    /**
     * 
     * @param patternString
     * @return
     */
	public String getAnnotations(String patternString) {

		List<String> sentenceTokens = new ArrayList<String>();
		
		List<List<CoreLabel>> classifiedString = classifier.classify(patternString);
		for ( List<CoreLabel> sentence : classifiedString) {
		
			for ( CoreLabel word : sentence ) {
				
				sentenceTokens.add(NamedEntityTagNormalizer.NAMED_ENTITY_TAG_MAPPINGS.get(word.get(AnswerAnnotation.class)));
			}
		}
		return StringUtils.join(sentenceTokens, " ");
	}
}
