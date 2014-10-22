package org.aksw.autosparql.commons.qald;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.autosparql.commons.Constants;
import org.aksw.autosparql.commons.nlp.ner.StanfordNLPNamedEntityRecognition;
import org.aksw.autosparql.commons.nlp.pos.StanfordNLPPartOfSpeechTagger;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.autosparql.commons.qald.uri.GoldEntity;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 *
 */
public class QaldLoader {

	public static void main(String[] args) {

		for ( Question q : QaldLoader.loadAndSerializeQuestions(Arrays.asList("en"), "de_wac_175m_600.crf.ser.gz", "english.all.3class.distsim.crf.ser.gz",
																"german-dewac.tagger", "english-left3words-distsim.tagger", true)) {
			System.out.println(q);;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Question> loadAndSerializeQuestions(List<String> supportedLanguages, String germanNerModel, String englishNerModel, String germanPosModel, String englishPosModel, boolean override) {

		if ( !override )
			if ( QaldLoader.class.getResource("/qald/questions.ser") != null )
				return (List<Question>) SerializationUtils.deserialize(QaldLoader.class.getResourceAsStream("/qald/questions.ser"));

		List<Question> questions = new ArrayList<Question>();

		StanfordNLPNamedEntityRecognition deNerTagger	= null;
		StanfordNLPPartOfSpeechTagger dePosTagger		= null;
		StanfordNLPNamedEntityRecognition enNerTagger	= null;
		StanfordNLPPartOfSpeechTagger enPosTagger		= null;

		if ( supportedLanguages.contains("de") ) {

			deNerTagger = new StanfordNLPNamedEntityRecognition(QaldLoader.class.getResource("/models/de/ner/" + germanNerModel).getFile());
			dePosTagger = new StanfordNLPPartOfSpeechTagger(QaldLoader.class.getResource("/models/de/pos/" + germanPosModel).getFile());
    	}
    	if ( supportedLanguages.contains("en") ) {

    		enNerTagger = new StanfordNLPNamedEntityRecognition(QaldLoader.class.getResource("/models/en/ner/" + englishNerModel).getFile());
    		enPosTagger = new StanfordNLPPartOfSpeechTagger(QaldLoader.class.getResource("/models/en/pos/" + englishPosModel).getFile());
    	}

		try {

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(QaldLoader.class.getResource("/qald/dbpedia-train.xml").getFile()));
            doc.getDocumentElement().normalize();
            NodeList questionNodes = doc.getElementsByTagName("question");

            for( int i = 0; i < questionNodes.getLength(); i++){

            	Question question = new Question();
            	Element questionNode = (Element) questionNodes.item(i);

            	question.id = Integer.valueOf(questionNode.getAttribute("id"));
            	question.answerType = questionNode.getAttribute("answerType");
            	question.aggregation = Boolean.valueOf(questionNode.getAttribute("aggregation"));
            	question.onlydbo = Boolean.valueOf(questionNode.getAttribute("onlydbo"));

                //Read question
            	NodeList nlrs = questionNode.getElementsByTagName("string");
            	for ( int j = 0; j < nlrs.getLength() ; j++) {

            		String lang = ((Element) nlrs.item(j)).getAttribute("lang");
            		if (supportedLanguages.contains(lang))
            			question.languageToQuestion.put(lang, ((Element) nlrs.item(j)).getTextContent().trim());
            	}

            	// read keywords
            	NodeList keywords = questionNode.getElementsByTagName("keywords");
            	for ( int j = 0; j < keywords.getLength() ; j++) {

            		String lang = ((Element) keywords.item(j)).getAttribute("lang");
            		if ( supportedLanguages.contains(lang))
            			question.languageToKeywords.put(lang, Arrays.asList(((Element) keywords.item(j)).getTextContent().trim().split(", ")));
            	}

                // Read SPARQL query
            	question.sparqlQuery = ((Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
            	// check if OUT OF SCOPE marked
            	question.outOfScope = question.sparqlQuery.toUpperCase().contains("OUT OF SCOPE");

            	for ( String goldEntity : StringUtils.split(questionNode.getElementsByTagName("enEntities").item(0).getTextContent(), ",")) {

            		String[] parts = goldEntity.split(":");
            		question.goldEntites.get("en").add(new GoldEntity(("http://dbpedia.org/resource/" + parts[0]).trim(), parts[1].replace("_", ",").replace("[", "").replace("]", "").trim()));
            	}
            	for ( String goldEntity : StringUtils.split(questionNode.getElementsByTagName("deEntities").item(0).getTextContent(), ",")) {

            		String[] parts = goldEntity.split(":");
            		question.goldEntites.get("de").add(new GoldEntity(("http://dbpedia.org/resource/" + parts[0]).trim(), parts[1].replace("_", ",").replace("[", "").replace("]", "").trim()));
            	}

            	if ( supportedLanguages.contains("de") ) {

            		question.languageToNamedEntites.put("de", getEntities(deNerTagger, question.languageToQuestion.get("de")));
                	question.languageToNounPhrases.put("de", getEntities(dePosTagger, question.languageToQuestion.get("de"), false));
            	}
            	if ( supportedLanguages.contains("en") ) {

            		question.languageToNamedEntites.put("en", getEntities(enNerTagger, question.languageToQuestion.get("en")));
                	question.languageToNounPhrases.put("en", getEntities(enPosTagger, question.languageToQuestion.get("en"), false));

                	String posTaggedQuestion = enPosTagger.getAnnotatedSentence(question.languageToQuestion.get("en"));

                	Iterator<Entity> iter = question.languageToNamedEntites.get("en").iterator();
                	while ( iter.hasNext() ) {
                		Entity e = iter.next();
                		for ( String part : e.label.split(" ") ) {

                				String posPart = posTaggedQuestion.substring(posTaggedQuestion.indexOf(part), posTaggedQuestion.indexOf(part) + part.length() + 4);
                    			if ( posPart.contains("_JJ") ) {

                    				e.label = e.label.replace(part, "").trim();
//                    				System.out.println(e.label);
                    			}
                    			if ( posPart.contains("_VBZ") ) {

                    				e.label = e.label.replace(part, "").trim();
//                    				System.out.println(e.label);
                    			}
                		}
                		if ( e.label.isEmpty() ) iter.remove();
                	}
            	}

            	questions.add(question);
            }

            SerializationUtils.serialize((Serializable) questions, new FileOutputStream(new File("src/main/resources/qald/questions.ser")));
		}
		catch (DOMException e) {
	            e.printStackTrace();
	    }
		catch (ParserConfigurationException e) {
	            e.printStackTrace();
	    }
		catch (SAXException e) {
	            e.printStackTrace();
	    }
		catch (IOException e) {
	            e.printStackTrace();
	    }
		return questions;
	}

	private static List<Entity> getEntities(StanfordNLPPartOfSpeechTagger posTagger, String questionString, boolean useNNP) {

		List<Entity> entities = getEntities(mergePartOfSpeechTagsInSentences(posTagger.getAnnotatedSentence(questionString), useNNP));
		if ( useNNP ) {

			Iterator<Entity> iter = entities.iterator();
			while ( iter.hasNext() ) {

				Entity next = iter.next();
				if ( next.type.equals(Constants.NOUN)) iter.remove();
			}
		}

		return entities;
	}

	private static List<Entity> getEntities(StanfordNLPNamedEntityRecognition nerTagger, String questionString) {

		return getEntities(mergeNamedEntityTagsInSentences(nerTagger.getAnnotatedSentence(questionString)));
	}

	/**
     *
     * @param mergedTaggedSentence
     * @return
     */
    private static List<Entity> getEntities(List<String> mergedTaggedSentence){

        List<Entity> entities = new ArrayList<Entity>();
        for (int i = 0; i < mergedTaggedSentence.size(); i++) {

        	int index = Math.max(1, i-1);
        	String the = "";
        	if ( mergedTaggedSentence.get(index).equals("The_OTHER") ) the = "The ";

        	String entity = mergedTaggedSentence.get(i);

            if (entity.endsWith("_PERSON") ) entities.add(new Entity(the + entity.replace("_PERSON", ""), Constants.PERSON));
            if (entity.endsWith("_MISC")) entities.add(new Entity(the + entity.replace("_MISC", ""), Constants.MISC));
            if (entity.endsWith("_PLACE")) entities.add(new Entity(the + entity.replace("_PLACE", ""), Constants.PLACE));
            if (entity.endsWith("_ORGANIZATION")) entities.add(new Entity(the + entity.replace("_ORGANIZATION", ""), Constants.ORGANIZATION));
            if (entity.endsWith("_NN")) entities.add(new Entity(the + entity.replace("_NN", ""), Constants.NOUN));
            if (entity.endsWith("_NNP")) entities.add(new Entity(the + entity.replace("_NNP", ""), Constants.ENTITY));
        }

        return entities;
    }

    /**
     *
     * @param annotatedSentence
     * @return
     */
    private static List<String> mergePartOfSpeechTagsInSentences(String annotatedSentence, boolean useNNP) {

    	List<String> tokens = new ArrayList<String>();
        String lastToken = "";
        String lastTag = "";
        String currentTag = "";
        String newToken = "";

        String taggedSentence = useNNP ? annotatedSentence.replace("_NNPS", "_NNP") : annotatedSentence.replace("_NNS", "_NN");
        String delimiter = useNNP ? "_NNP" : "_NN";

        for (String currentToken : taggedSentence.split(" ")) {

            currentTag = currentToken.substring(currentToken.lastIndexOf("_") + 1);

            // we need to check for the previous token's tag
            if (currentToken.endsWith(delimiter)) {

                // we need to merge the cell
                if (currentTag.equals(lastTag)) {

                    newToken = lastToken.substring(0, lastToken.lastIndexOf("_")) + " " + currentToken;
                    tokens.set(tokens.size() - 1, newToken);
                }
                // different tag found so just add it
                else
                    tokens.add(currentToken);
            }
            else {

                // add the current token
                tokens.add(currentToken);
            }
            // update for next iteration
            lastToken = tokens.get(tokens.size() - 1);
            lastTag = currentTag;
        }
        return tokens;
	}

    /**
     *
     */
    public static List<String> mergeNamedEntityTagsInSentences(String nerTaggedSentence) {

        List<String> tokens = new ArrayList<String>();
        String lastToken = "";
        String lastTag = "";
        String currentTag = "";
        String newToken = "";

        for (String currentToken : nerTaggedSentence.split(" ")) {

            currentTag = currentToken.substring(currentToken.lastIndexOf("_") + 1);

            // we need to check for the previous token's tag
            if (!currentToken.endsWith("_OTHER")) {

                // we need to merge the cell
                if (currentTag.equals(lastTag)) {

                    newToken = lastToken.substring(0, lastToken.lastIndexOf("_")) + " " + currentToken;
                    tokens.set(tokens.size() - 1, newToken);
                }
                // different tag found so just add it
                else tokens.add(currentToken);
            }
            else {

                // add the current token
                tokens.add(currentToken);
            }
            // update for next iteration
            lastToken = tokens.get(tokens.size() - 1);
            lastTag = currentTag;
        }

        return tokens;
    }
}
