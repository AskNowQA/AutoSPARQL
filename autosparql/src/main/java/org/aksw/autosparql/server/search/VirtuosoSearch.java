package org.aksw.autosparql.server.search;

import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class VirtuosoSearch extends SPARQLSearch{
	
	private static Logger logger = Logger.getLogger(VirtuosoSearch.class);
	
	protected String queryTemplate = "SELECT DISTINCT(?uri) WHERE {\n" +
			"?uri a ?type.\n" + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
			"?label bif:contains '%s'}\n" +
			"LIMIT %d OFFSET %d";
	
	protected String countQueryTemplate = "SELECT (COUNT(DISTINCT ?uri) AS ?cnt) WHERE {" +
			"?uri a ?type. " + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
			"?label bif:contains '%s'} ";
	
	protected String exampleQueryTemplate = "SELECT DISTINCT(?uri) ?label ?imageURL ?comment WHERE {\n" +
			"?uri a ?type.\n" + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
			"?label bif:contains '%s'\n" +
			"FILTER(LANGMATCHES(LANG(?label), \"en\"))\n" +
			"OPTIONAL{?uri <http://dbpedia.org/ontology/thumbnail> ?imageURL.}\n" + 
			"OPTIONAL{?uri <http://www.w3.org/2000/01/rdf-schema#comment> ?comment. FILTER(LANGMATCHES(LANG(?comment), \"en\"))}\n" +
			"} " +
			"LIMIT %d OFFSET %d";
	
	
	public VirtuosoSearch(SparqlEndpoint endpoint) {
		super(endpoint);
	}
	
	public VirtuosoSearch(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		super(endpoint, cache);
	}
	
	public VirtuosoSearch(SparqlEndpoint endpoint, ExtractionDBCache cache, QuestionProcessor keywordExtractor) {
		super(endpoint, cache, keywordExtractor);
	}
	
	protected String buildExamplesQuery(String searchTerm, int offset){
		return String.format(exampleQueryTemplate, searchTerm, limit, offset);
	}
	
	protected String buildResourcesQuery(String searchTerm, int offset){
		return String.format(queryTemplate, searchTerm, limit, offset);
	}
	
	protected String buildCountQuery(String searchTerm){
		return String.format(countQueryTemplate, searchTerm);
	}
	
	public static void main(String[] args) {
			System.out.println(new VirtuosoSearch(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), new ExtractionDBCache("cache")).getResources("Leipzig"));
			System.out.println(new VirtuosoSearch(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), new ExtractionDBCache("cache")).getExamples("Leipzig"));
	}
	

}
