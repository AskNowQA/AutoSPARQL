package org.aksw.autosparql.commons.nlp.pos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.aksw.autosparql.commons.nlp.sbd.StanfordNLPSentenceBoundaryDisambiguation;
import org.apache.commons.lang3.StringUtils;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * 
 * @author gerb
 */
public final class StanfordNLPPartOfSpeechTagger {

	private static final String PART_OF_SPEECH_TAG_DELIMITER = "_";
	
    private MaxentTagger tagger;
    private StanfordNLPSentenceBoundaryDisambiguation sbd   = new StanfordNLPSentenceBoundaryDisambiguation();
    
	public StanfordNLPPartOfSpeechTagger(String classifierPath) {
		
		try {
			
		    // this is used to surpress the "error" messages from stanford etc.
//            PrintStream standardErrorStream = System.err;
//            System.setErr(new PrintStream(new ByteArrayOutputStream()));

			this.tagger = new MaxentTagger(classifierPath);
			
			// revert to original standard error stream
//            System.setErr(standardErrorStream);
		}
		catch (ClassNotFoundException e) {
			
			e.printStackTrace();
			throw new RuntimeException("Wrong classifier specified in config file.", e);
		}
		catch (IOException e) {
			
			e.printStackTrace();
			throw new RuntimeException("Could not read trained model!", e);
		}
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 */
	public String getAnnotatedSentence(String string) {

		List<String> sentence = new ArrayList<String>();
		for ( String taggedWord : tagger.tagString(string).split(" ")) {
			
			int lastIndex	= taggedWord.lastIndexOf("/");
			if ( lastIndex == -1 ) lastIndex = taggedWord.lastIndexOf("_"); 
			String posTag	= taggedWord.substring(lastIndex + 1);
			String token	= taggedWord.substring(0, lastIndex);
			
			sentence.add(token + PART_OF_SPEECH_TAG_DELIMITER + posTag);
		}
		return StringUtils.join(sentence, " ");
	}

    /**
     * 
     * @param string
     * @return
     */
	public String getAnnotations(String string) {

		// add the surfaceForms ot the front/end to improve accuracy and tag it 
		String[] taggedSentence = this.getAnnotatedSentence(string).split(" ");
		// remove the words, to only have the pos tags
		List<String> tags = new ArrayList<String>();
		for ( String s : taggedSentence ) {
			
			tags.add(s.substring(s.lastIndexOf(PART_OF_SPEECH_TAG_DELIMITER) + 1));
		}
		return StringUtils.join(tags, " ");
	}
	
	/**
     * 
     * @param sentences
     * @return
     */
    public String getAnnotatedSentences(String sentences) {
        
        StringBuffer buffer = new StringBuffer();
        for ( String sentence : sbd.getSentences(sentences) ) {
            
            buffer.append(getAnnotatedSentence(sentence)).append("\n");
        }
            
                
        return buffer.toString();
    }
    
	/**
	 * 
	 * @param sentence
	 * @return
	 */
	public List<String> getNounPhrases(String sentence) {

	    String[] taggedSentence    = this.getAnnotatedSentence(sentence).split(" ");
	    List<String> nounPhrases   = new ArrayList<String>();
	    
	    List<String> currentNounPhrase = new ArrayList<String>();
	    
	    for ( String taggedWord : taggedSentence) {
	        
	        // do we have a proper noun in singular or plural
            if ( taggedWord.matches(".+_NNPS?") ) {
                
                currentNounPhrase.add(taggedWord.substring(0, taggedWord.indexOf("_")));
            }
            else {
                
                if ( !currentNounPhrase.isEmpty() ) {
                    
                    nounPhrases.add(StringUtils.join(currentNounPhrase, " "));
                    currentNounPhrase = new ArrayList<String>();
                }
            }
	    }
	    
	    return nounPhrases;
	}
}
