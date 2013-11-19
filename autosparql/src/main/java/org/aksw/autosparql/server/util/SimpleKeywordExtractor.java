package org.aksw.autosparql.server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class SimpleKeywordExtractor implements KeywordExtractor{
	
	private static final Logger logger = Logger.getLogger(SimpleKeywordExtractor.class);
	
	private MaxentTagger tagger;
	
	public SimpleKeywordExtractor(String taggerModelPath) {
		 try {
			tagger = new MaxentTagger(this.getClass().getClassLoader().getResource("de/simba/ner/models/left3words-wsj-0-18.tagger").toString());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> extractKeywords(String phrase) {
		List<String> nouns = parseForNouns(phrase);
		
		return nouns;
	}
	
	/** Get nouns from the string s
    *
    * @param s
    * @return
    */
   private List<String> parseForNouns(String s) {
       logger.info("Initial query = "+s);
       List<String> result = new ArrayList<String>();
       try {
           File temp = File.createTempFile("999", null);
           //write input
           PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(temp.getAbsolutePath())));
           writer.println(s);
           writer.close();

           //tag and write output
           String buffer = "";
           List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(s)));
           for (List<HasWord> sentence : sentences) {
               ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
               for(TaggedWord taWo : tSentence){
               	System.out.println("Word:" + taWo.word() + " Tag: " + taWo.tag());
               }
               buffer = buffer + Sentence.listToString(tSentence, false);
           }

           logger.info("POS-tagged query = " + buffer);
           result = getNNs(buffer);
           //read strings
           //stem words
//           result = getStemmedWords(result);

       } catch (Exception e) {
           e.printStackTrace();
       }
       return result;
   }
   
   private List<String> getNNs(String buffer) {
	   List<String> result = new ArrayList<String>();
       String split[] = buffer.split(" ");
       String nameAndTag[], noun = "";
       for (int i = 0; i < split.length; i++) {
           nameAndTag = split[i].split("/");
           if (nameAndTag[1].startsWith("NN")) {
               noun = nameAndTag[0];
               i++;
               for (int j = i; j < split.length; j++) {
                   nameAndTag = split[j].split("/");
                   if (nameAndTag[1].startsWith("NN")) {
                       noun = noun + " " + nameAndTag[0];
                       i++;
                   } else {
                       break;
                   }
               }
           }
           if (!noun.equals("")) {
               result.add(noun);
               noun = "";
           }
       }
       return result;
   }

}
