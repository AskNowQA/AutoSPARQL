/**
 * 
 */
package org.aksw.autosparql.commons.uri;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.autosparql.commons.Constants;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

/**
 * @author gerb
 *
 */
public class UriDisambiguation {
	
	static SolrServer enSolrServer = new HttpSolrServer("http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_resources");
	static SolrServer deSolrServer = new HttpSolrServer("http://[2001:638:902:2010:0:168:35:138]:8080/solr/de_dbpedia_resources");
	private static Map<String,List<Resource>> labelToResources = new HashMap<String, List<Resource>>();
	public static Double APRIORI_PARAMETER = 0.2D;
//	public static Double CONTEXT_SIMILARTY_PARAMETER = 0.11D;
	public static Double STRING_SIMILARTY_PARAMETER = 0.5D;
	private static AbstractStringMetric metric = new Levenshtein();
	static Map<String,String>  nerTypeToUri = new HashMap<String, String>();
	public static int BONUS = 1;
	
	static {
		
		nerTypeToUri.put("PERSON", "http://dbpedia.org/ontology/Person");
		nerTypeToUri.put("ORGANIZATION", "http://dbpedia.org/ontology/Organisation");
		nerTypeToUri.put("PLACE", "http://dbpedia.org/ontology/Place");
	}
	
	public static void main(String[] args) {
		
		System.out.println(UriDisambiguation.getTopUris(UriDisambiguation.getUriCandidates("New Jersey", "en"), "New Jerysey", "en"));
	}
	
	/** @see getTopUris(List<Resource>, String, String, int) */
	public static List<Resource> getTopUris(List<Resource> candidateResources, String label, String language)
	{return getTopUris(candidateResources,label,language,3);}
	/**
	 * returns top n resources
	 * @param label
	 * @param language
	 * @return
	 */
	public static List<Resource> getTopUris(List<Resource> candidateResources, String label, String language, int n) {
		
		if ( candidateResources.isEmpty() ) {
			
			return new ArrayList<Resource>();
		}
		else {
			
			Map<Resource,Map<String,Double>> resourcesToScores = new HashMap<Resource,Map<String,Double>>();
			Map<String,Double> maxValues = new HashMap<String, Double>();
			maxValues.put(Constants.APRIORI_FEATURE, 0D);
			maxValues.put(Constants.STRING_SIMILARTY_FEATURE, 0D);
			
			for ( Resource candidateResource : candidateResources) {
			
				Double apriori = candidateResource.aprioriScore;
				Double stringsim = getStringSimilarityScore(label, candidateResource);
				
				Map<String,Double> scores = new HashMap<String,Double>();
				scores.put(Constants.APRIORI_FEATURE, apriori);
				scores.put(Constants.STRING_SIMILARTY_FEATURE, stringsim);
				
				if ( maxValues.get(Constants.APRIORI_FEATURE) <= apriori) maxValues.put(Constants.APRIORI_FEATURE, apriori);
				
				resourcesToScores.put(candidateResource, scores);
			}
			
			List<Resource> resources = new ArrayList<Resource>();
			
			for ( Map.Entry<Resource, Map<String,Double>> entry : resourcesToScores.entrySet()) {

				Map<String,Double> features = entry.getValue();
				Double apriori = (APRIORI_PARAMETER * features.get(Constants.APRIORI_FEATURE)) / maxValues.get(Constants.APRIORI_FEATURE);
				
				double score =  (!apriori.isNaN() && !apriori.isInfinite() ? apriori : 0) + 
						(STRING_SIMILARTY_PARAMETER * features.get(Constants.STRING_SIMILARTY_FEATURE));

				
				// useless features
				//  * bonus for rdf:type matches ner type
				//  * bonus for noun phrases in comments
				
				if ( entry.getKey().uri.contains("(") ) score -= BONUS;
				entry.getKey().score = score;
				
				resources.add(entry.getKey());
			}
			Collections.sort(resources, new Comparator<Resource>(){

				public int compare(Resource o1, Resource o2) {
					return o1.score > o2.score ? -1 : o1.score == o2.score ? 0 : 1;
				}
				
			});
			
			return resources.size() >= 3 ? resources.subList(0, 3) : resources;
		}
	}
	
	/**
	 * 
	 * @param q 
	 * @param label
	 * @return
	 */
	public static List<Resource> getUri(Question q, Entity entity, String language) {
		
		return getTopUris(getUriCandidates(q, entity.label, language), entity.label, language);
	}

	private static Double getStringSimilarityScore(String label, Resource candidateResource) {

		return (double) metric.getSimilarity(label, candidateResource.label);
	}

	/** Sorts the resources according to their score, descending (real disambiguation with contextcomes later). *
	 */
	public static List<Resource> getUriCandidates(String label, String language) {

		SolrServer server = language.equals("en") ? enSolrServer : language.equals("de") ? deSolrServer : enSolrServer;
		List<Resource> candidates = query(server, label);

		// if we have a word in plural an no resources have been found so far, try singular
		if ( label.endsWith("s") && candidates.isEmpty() ) candidates = query(server, label.replaceAll("s$", ""));
		// german solr query didnt return any results, so use the english one
		if ( language.equals("de") && candidates.isEmpty() ) candidates = query(enSolrServer, label);
		// remove the plural "n" of the first word
		if ( language.equals("de") && candidates.isEmpty() && label.split(" ")[0].endsWith("n") ) candidates = query(deSolrServer, label.replaceFirst("n ", " "));
		// uppercase all letters
		if ( language.equals("de") && candidates.isEmpty() ) candidates = query(deSolrServer, WordUtils.capitalize(label));
		// remove plural n
		if ( language.equals("de") && candidates.isEmpty() ) candidates = query(deSolrServer, label.replaceAll("n$", ""));
		
		labelToResources.put(label, candidates);
		
		return candidates;
	}
	
	public static List<Resource> query(SolrServer server, String label) {
		
		List<Resource> resources = new ArrayList<Resource>();
		
		SolrQuery query = new SolrQuery();
		query.setQuery("surfaceForms:\""+ label +"\"");
		query.addSortField("disambiguationScore", ORDER.desc);
		query.addField("surfaceForms");
		query.addField("uri");
		query.addField("label");
		query.addField("comment");
		query.addField("types");
		query.addField("dbpediaUri");
		query.addField("disambiguationScore");
		query.setRows(100);
		
		try {

			for ( SolrDocument doc : server.query(query).getResults()) {
				
				Resource res = new Resource();
				res.uri = (String) doc.getFieldValue("uri");
				res.uri = doc.getFieldValue("dbpediaUri") != null ? (String) doc.getFieldValue("dbpediaUri") : res.uri;
				res.label = (String) doc.getFieldValue("label");
				res.goldLabel = label;
				res.aprioriScore = (Double) doc.getFieldValue("disambiguationScore");
				res.surfaceForms = (List<String>) doc.getFieldValue("surfaceForms");
				res.comment = (String) doc.getFieldValue("comment");
				res.types = (List<String>) doc.getFieldValue("types");
				
				if ( res.uri.contains("(") ) {
					
					String contextPart = res.uri.substring(res.uri.indexOf("(") );
					res.context = Arrays.asList(contextPart.replace("(", "").replace(")", "").replace("_", " "));
				}
				else if ( res.uri.contains(",") ) {
					
					String contextPart = res.uri.substring(res.uri.indexOf(",") ).replace("_", " ");
					res.context = Arrays.asList(contextPart.replaceAll("^,", "").trim().split(","));
				}
				
				if ( !res.uri.trim().isEmpty() ) resources.add(res);
			}
		}
		catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return resources;
	}
	
	private static List<Resource> getUriCandidates(Question q, String label, String language) {

		String key = label + q.id + language;
		
		if ( labelToResources.containsKey(key) ) return labelToResources.get(key);
		else {
			
			List<Resource> resources = getUriCandidates(label, language);
			for ( Resource r : resources ) r.questionId = q.id + ""; 
			labelToResources.put(key, resources);
			return resources;
		}
	}
}
