package org.dllearner.autosparql.server.search;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.autosparql.client.model.Example;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQLSearch implements Search{
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
//	private KeywordExtractor keywordExtractor;
	private QuestionProcessor preprocessor;
	
	protected int limit = 10;
	
	protected String queryTemplate = "SELECT DISTINCT(?uri) WHERE {\n" +
			"?uri a ?type.\n" + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
			"FILTER(REGEX(STR(?label), '%s'))}\n" +
			"LIMIT %d OFFSET %d";
	
	protected String countQueryTemplate = "SELECT (COUNT(DISTINCT ?uri) AS ?cnt) WHERE {" +
			"?uri a ?type. " + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label. " +
			"FILTER(REGEX(?label, '%s'))} ";
	
	protected String exampleQueryTemplate = "SELECT DISTINCT(?uri) ?label ?imageURL ?comment WHERE {\n" +
			"?uri a ?type.\n" + 
			"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
			"FILTER(REGEX(?label, '%s'))\n" +
			"FILTER(LANGMATCHES(LANG(?label), \"en\"))\n" +
			"OPTIONAL{?uri <http://dbpedia.org/ontology/thumbnail> ?imageURL.}\n" + 
			"OPTIONAL{?uri <http://www.w3.org/2000/01/rdf-schema#comment> ?comment. FILTER(LANGMATCHES(LANG(?comment), \"en\"))}\n" +
			"} " +
			"LIMIT %d OFFSET %d";
	
	public SPARQLSearch(SparqlEndpoint endpoint) {
		this(endpoint, new ExtractionDBCache("cache"));
	}
	
	public SPARQLSearch(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
//	public SPARQLSearch(SparqlEndpoint endpoint, ExtractionDBCache cache, KeywordExtractor keywordExtractor) {
//		this.endpoint = endpoint;
//		this.cache = cache;
//		this.keywordExtractor = keywordExtractor;
//	}
	
	public SPARQLSearch(SparqlEndpoint endpoint, ExtractionDBCache cache, QuestionProcessor preprocessor) {
		this.endpoint = endpoint;
		this.cache = cache;
		this.preprocessor = preprocessor;
	}

	@Override
	public List<String> getResources(String searchTerm) {
		return getResources(searchTerm, 0);
	}

	@Override
	public List<String> getResources(String searchTerm, int offset) {
		List<String> resources = new ArrayList<String>();
		
		String query = buildResourcesQuery(searchTerm, offset);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			RDFNode uriNode = qs.get("uri");
			if(uriNode.isURIResource()){
				resources.add(uriNode.asResource().getURI());
			}
		}
		return resources;
	}
	
	@Override
	public List<Example> getExamples(String searchTerm) {
		return getExamples(searchTerm, 0);
	}

	@Override
	public List<Example> getExamples(String searchTerm, int offset) {
		List<Example> examples = new ArrayList<Example>();
		
		String query = buildExamplesQuery(searchTerm, offset);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		String uri;
		String label;
		String imageURL;
		String comment;
		QuerySolution qs;
		while(rs.hasNext()){
			uri = "";
			label = "";
			imageURL = "";
			comment = "";
			
			qs = rs.next();
			uri = qs.getResource("uri").getURI();
			if(qs.getLiteral("label") != null){
				label = qs.getLiteral("label").getLexicalForm();
			}
			if(qs.getResource("imageURL") != null){
				imageURL = qs.getResource("imageURL").getURI();
			}
			if(qs.getLiteral("comment") != null){
				comment = qs.getLiteral("comment").getLexicalForm();
			}
			
			examples.add(new Example(uri, label, imageURL, comment));
		}
		return examples;
	}

	@Override
	public int getTotalHits(String searchTerm) {
		int cnt = -1;
		String query = buildCountQuery(searchTerm);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		if(rs.hasNext()){
			cnt = rs.next().get("cnt").asLiteral().getInt();
		}
		return cnt;
	}

	@Override
	public void setHitsPerPage(int hitsPerPage) {
		limit = hitsPerPage;
	}
	
	@Override
	public void setQuestionPreprocessor(QuestionProcessor preprocessor) {
		this.preprocessor = preprocessor;
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
		System.out.println(new SPARQLSearch(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), new ExtractionDBCache("cache")).getResources("Leipzig"));
		System.out.println(new SPARQLSearch(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), new ExtractionDBCache("cache")).getExamples("Leipzig"));
	}
	

}
