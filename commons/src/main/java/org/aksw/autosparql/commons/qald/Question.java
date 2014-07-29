package org.aksw.autosparql.commons.qald;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.autosparql.commons.qald.uri.GoldEntity;
import org.apache.commons.lang3.StringUtils;

public class Question implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9130793012431486456L;
	
	public Integer id;
	public String answerType;
	public Boolean aggregation;
	public Boolean onlydbo;
	public String sparqlQuery;
	public Boolean outOfScope;
	public Map<String,String> languageToQuestion = new LinkedHashMap<String,String>();
	public Map<String,List<String>> languageToKeywords = new LinkedHashMap<String,List<String>>();
	
	public Map<String,List<Entity>> languageToNamedEntites = new LinkedHashMap<String, List<Entity>>();
	public Map<String,List<Entity>> languageToNounPhrases = new LinkedHashMap<String, List<Entity>>();
	public Map<String,List<GoldEntity>> goldEntites = new HashMap<String,List<GoldEntity>>();
	
	public Question() {
		
		goldEntites.put("en", new ArrayList<GoldEntity>());
		goldEntites.put("de", new ArrayList<GoldEntity>());
	}
	
	@Override
	public String toString(){
		
		String output = String.format("id: %s answerType: %s aggregation: %s onlydbo: %s\n", id, answerType, aggregation, onlydbo);
		for ( Map.Entry<String, String> entry : languageToQuestion.entrySet() ) {
			output +=  "\t"+entry.getKey()+"\tQuestion: " + entry.getValue() + "\n";
			output +=  "\t\tKeywords: " + StringUtils.join(languageToKeywords.get(entry.getKey()), ", ") + "\n";
			output +=  "\t\tGold-Entities: " + StringUtils.join(goldEntites, ", ") + "\n";
			if (languageToNamedEntites.containsKey(entry.getKey())) output +=  "\t\tEntities: " + StringUtils.join(languageToNamedEntites.get(entry.getKey()), ", ") + "\n";
			if (languageToNounPhrases.containsKey(entry.getKey())) output +=  "\t\tNouns: " + StringUtils.join(languageToNounPhrases.get(entry.getKey()), ", ") + "\n";
		}
		output += "SPARQL: " +sparqlQuery;
		
		return output;
	}
}
