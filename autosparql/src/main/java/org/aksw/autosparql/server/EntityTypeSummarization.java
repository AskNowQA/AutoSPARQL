package org.aksw.autosparql.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

public class EntityTypeSummarization {
	
	private static final String WIKIPEDIA_BASE = "http://en.wikipedia.org/wiki/";
	
	private SparqlEndpoint endpoint;
	
	private Map<String, Integer> contentLengthCache;
	
	
	public EntityTypeSummarization(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		
		contentLengthCache = new HashMap<String, Integer>();
	}
	
	public Set<String> getSummarizedTypes(String resource){
		Set<String> types = getTypesOfResource(resource);
		System.out.println(types);
		for(String type : types){
			computeAverageContentLengthOfWikipediaArticles(type);
		}
		return null;
	}
	
	
	public void computeAverageContentLengthOfWikipediaArticles(String type){
		System.out.println(type);
		Set<String> resources = getResources(type);
		int cnt = resources.size();
		int sum = 0;
		for(String resource : resources){
			sum += getContentLengthOfWikipediaArticle(resource);
		}
		
		int avg = sum / cnt;
		
		System.out.println("Average content length: " + avg);
		
	}
	
	private Set<String> getTypesOfResource(String resource){
		String query = "SELECT ?type WHERE {<" + resource + "> a ?type} LIMIT 100";
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		Set<String> types = new HashSet<String>();
		while(rs.hasNext()){
			types.add(rs.next().getResource("type").getURI());
		}
		return types;
	}
	
	private int getContentLengthOfWikipediaArticle(String resource){
		int contentLength = -1;
		try {
			String wikipediaURL = WIKIPEDIA_BASE + resource.substring(resource.lastIndexOf("/") + 1);
			URL url = new URL(wikipediaURL);
			URLConnection con = url.openConnection();
			contentLength = con.getContentLength();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentLength;
	}
	
	private Set<String> getResources(String type){
		String query = "SELECT ?uri WHERE {?uri a <" + type + ">} LIMIT 10";
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		Set<String> resources = new HashSet<String>();
		while(rs.hasNext()){
			resources.add(rs.next().getResource("uri").getURI());
		}
		return resources;
	}

	public static void main(String[] args) throws IOException {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		EntityTypeSummarization sum = new EntityTypeSummarization(endpoint);
		sum.getSummarizedTypes("http://dbpedia.org/resource/Angela_Merkel");
		
	}
}
