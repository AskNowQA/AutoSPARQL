package org.aksw.autosparql.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.utilities.MapUtils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * This class is used to get the most important classes of a given URI.
 * It was especially made for DBpedia as the content of Wikipedia articles is taken into account.
 * @author Lorenz Buehmann
 *
 */
public class EntityTypeSummarization {
	
	private static final String WIKIPEDIA_BASE = "http://en.wikipedia.org/wiki/";
	
	private SparqlEndpoint endpoint;
	
	private Map<String, Integer> contentLengthCache;

	private static final int maxNrOfResourcesPerClass = 100;
	private static final int maxNrOfDownloadThreads = 100;
	
	//can be enabled to reduce the effect of outliers
	private boolean useWinsorization = true;
	private double percentileInPercent = 95;//set to 95th percentile, i.e. each value above the percentile is set to the value of the percentile
	
	public EntityTypeSummarization(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		
		contentLengthCache = new HashMap<String, Integer>();
	}
	
	public Set<String> getSummarizedTypes(String resource){
		Set<String> types = getTypesOfResource(resource);
		Map<String, Double> type2Score = new TreeMap<String, Double>();
		for(String type : types){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			type2Score.put(type, computeAverageContentLengthOfWikipediaArticles(type));
		}
		List<Entry<String, Double>> sortedByValues = MapUtils.sortByValues(type2Score);
		for (Entry<String, Double> entry : sortedByValues) {
			System.out.println(entry.getValue() + "\t" + entry.getKey());
		}
		return null;
	}
	
	/**
	 * Given a DBpedia class it returns the average length of the content of the corresponding Wikipedia articles
	 * by analyzing a random sample of resources of the given class.
	 * @param type the DBpedia class
	 */
	public double computeAverageContentLengthOfWikipediaArticles(String type){
		Set<String> resources = getResources(type);
		int cnt = resources.size();
		final List<Integer> values = Collections.synchronizedList(new ArrayList<Integer>());
		ExecutorService tp = Executors.newFixedThreadPool(Math.min(maxNrOfDownloadThreads, resources.size()));
		for(final String resource : resources){
			tp.submit(new Runnable() {
				
				@Override
				public void run() {
					int length = getContentLengthOfWikipediaArticle(resource);
					values.add(length);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		tp.shutdown();
		try {
			tp.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(useWinsorization){
			winsorize(values);
		}
		int sum = 0;
		for (Integer val : values) {
			sum += val;
		}
		double avg = sum / (double)cnt;
		
		return avg;
	}
	
	/**
	 * Winsorize a given list of Double numbers.
	 * @param values
	 */
	private void winsorize(List<Integer> values){
		//compute 95th percentile
		int percentile = (int) Math.round(percentileInPercent/100d * values.size() + 1/2d);
		//sort values
		Collections.sort(values);
		//get the value at percentile rank
		Integer max = values.get(percentile-1);
		//set all values after to max
		for (int i = percentile; i < values.size(); i++) {
			values.set(i, max);
		}
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
	
	private int getContentLengthOfWikipediaArticle(String resourceURI){
		int contentLength = -1;
		try {
			String wikipediaURL = getWikipediaURL(resourceURI);
			URL url = new URL(wikipediaURL);
			URLConnection con = url.openConnection();
			contentLength = con.getContentLength();
			if(contentLength == -1){//i.e. server returned no content length filed in HTTP header
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			    String inputLine;
			    while ((inputLine = in.readLine()) != null) {
			    	contentLength += inputLine.getBytes().length;
			    }
			    in.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentLength;
	}
	
	private String getWikipediaURL(String resourceURI){
		return WIKIPEDIA_BASE + resourceURI.substring(resourceURI.lastIndexOf("/") + 1);
	}
	
	/**
	 * Get resource that have a corresponding Wikipedia article.
	 * @param type
	 * @return
	 */
	private Set<String> getResources(String type){
		String query = "SELECT ?uri WHERE {?uri a <" + type + ">. ?uri <http://xmlns.com/foaf/0.1/isPrimaryTopicOf>.} LIMIT " + maxNrOfResourcesPerClass;
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
