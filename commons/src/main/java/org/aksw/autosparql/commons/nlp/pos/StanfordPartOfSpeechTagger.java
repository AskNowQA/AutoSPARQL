package org.aksw.autosparql.commons.nlp.pos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import com.aliasi.tag.Tagging;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.JavaUtilLoggingAdaptor.RedwoodHandler;
import edu.stanford.nlp.util.logging.Redwood.RedwoodChannels;
import edu.stanford.nlp.util.logging.Redwood;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class StanfordPartOfSpeechTagger implements PartOfSpeechTagger{

	/** if you only use it single threadedly just use the singleton to save initialization time */	
	public static final StanfordPartOfSpeechTagger INSTANCE = StanfordPartOfSpeechTagger.INSTANCE;

	private StanfordCoreNLP pipeline;
	
	protected StanfordPartOfSpeechTagger(){
		RedwoodConfiguration.empty().apply();		
		Redwood.log("test redwood");
		
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos");
	    pipeline = new StanfordCoreNLP(props);
	}
	
	@Override
	public String getName() {
		return "Stanford POS Tagger";
	}

	@Override
	public String tag(String text) {
		String out = "";
		
	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = token.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = token.get(PartOfSpeechAnnotation.class);
	           
	            out += " " + word + "/" + pos;
	          }
	    }
		
		return out.trim();
	}
	
	

	@Override
	public List<String> tagTopK(String sentence) {
		return Collections.singletonList(tag(sentence));
	}
	
	public List<String> getTags(String text){
		List<String> tags = new ArrayList<String>();
		
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = token.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = token.get(PartOfSpeechAnnotation.class);
	           
	            tags.add(pos);
	          }
	    }
		
		return tags;
	}
	
	@Override
	public Tagging<String> getTagging(String text){
		List<String> tokenList = new ArrayList<String>();
		List<String> tagList = new ArrayList<String>();
		
		// create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	            String word = token.get(TextAnnotation.class);
	            // this is the POS tag of the token
	            String pos = token.get(PartOfSpeechAnnotation.class);
	           
	            tokenList.add(word);
				tagList.add(pos);
	          }
	    }
		
		return new Tagging<String>(tokenList, tagList);
	}
}
