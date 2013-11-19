package org.aksw.autosparql.server.search;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.aksw.autosparql.server.Defaults;
import org.aksw.autosparql.server.util.ExtractionDBCacheUtils;
import org.aksw.autosparql.shared.Example;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLPropertiesIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Options;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class TBSLSearch implements Search
{
	private static Logger log = Logger.getLogger(TBSLSearch.class);		
	private final static Options options; 
	public final static String SOLR_DBPEDIA_RESOURCES;
	public final static String SOLR_DBPEDIA_CLASSES;
	public final static String SOLR_DBPEDIA_PROPERTIES;
	static
	{
		InputStream optionStream = TBSLSearch.class.getResourceAsStream("tbsl.properties");
		try{options = new Options(optionStream);} catch (Exception e) {throw new RuntimeException("Problem loading properties from resource "+optionStream.toString()+". Could not initialize TBSLSearch.",e);};				
		SOLR_DBPEDIA_RESOURCES = options.get("solr.resources.url");
		SOLR_DBPEDIA_CLASSES = options.get("solr.classes.url");			
		SOLR_DBPEDIA_PROPERTIES = options.get("solr.properties.url");
	}	

	private static final int LIMIT = 10;
	private static final int OFFSET = 0;
 
	private final String QUERY_PREFIX = "";//"Give me all ";

	private final SPARQLTemplateBasedLearner2 tbsl;
	private final SPARQLEndpointEx endpoint;
	private String learnedQuery = null;

	public String learnedQuery() {return learnedQuery;}

	static class POSTaggerHolder
	{
		static {log.debug("initializing POS tagger...");}
		public static final PartOfSpeechTagger pos = new ApachePartOfSpeechTagger();
	}

	static class WordNetHolder
	{
		protected static final String wordNetFilename = "tbsl/wordnet_properties.xml";
		static {log.debug("initializing WordNet...");}
		public static final WordNet wordNet = new WordNet(wordNetFilename);
	}

	private static Map<List<String>,TBSLSearch> instances = new HashMap<List<String>,TBSLSearch>();

	private static TBSLSearch getInstance(final SPARQLEndpointEx endpoint,Index resourcesIndex, Index classesIndex, Index propertiesIndex,ExtractionDBCache cache)
	{
		synchronized(instances)
		{
			TBSLSearch search;
			List<String> key = Arrays.asList(endpoint.getURL().toString(),endpoint.getDefaultGraphURIs().toString());
			if((search=instances.get(key))!=null) {return search;}
			instances.put(key,search=new TBSLSearch(endpoint,resourcesIndex,classesIndex,propertiesIndex,cache));
			return search;
		}
	}

	public static TBSLSearch getDBpediaInstance()
	{
		SPARQLEndpointEx endpoint = null;
		try
		{
			endpoint = new SPARQLEndpointEx(
					new URL(Defaults.endpointURL()),
					Collections.singletonList(Defaults.graphURL()),Collections.<String>emptyList(),"","",Collections.<String>emptySet());
			return getInstance(endpoint,new SOLRIndex(SOLR_DBPEDIA_RESOURCES),new SOLRIndex(SOLR_DBPEDIA_CLASSES),new SOLRIndex(SOLR_DBPEDIA_PROPERTIES)
			,ExtractionDBCacheUtils.getDBpediaCache());
		}
		catch (MalformedURLException e)
		{log.fatal("Couldn't initialize SPARQL endpoint \""+Defaults.endpointURL()+"\"", e);throw new RuntimeException(e);}
		catch (SQLException e) {throw new RuntimeException("Could not get extraction cache.",e);}	
	}

	public static TBSLSearch getOxfordInstance()
	{
		SPARQLEndpointEx endpoint;
		try
		{
			endpoint = new SPARQLEndpointEx(new URL(Defaults.oxfordEndpointURL()), Collections.singletonList(Defaults.oxfordGraphURL()), Collections.<String>emptyList(),"","",Collections.<String>emptySet());

			SPARQLIndex resourceIndex = new SPARQLIndex(endpoint);
			SPARQLClassesIndex classIndex = new SPARQLClassesIndex(endpoint);
			SPARQLPropertiesIndex propertyIndex = new SPARQLPropertiesIndex(endpoint);

			return getInstance(endpoint,resourceIndex,classIndex,propertyIndex,ExtractionDBCacheUtils.getOxfordCache());
		}
		catch (MalformedURLException e)
		{log.fatal("Couldn't initialize SPARQL endpoint \""+Defaults.endpointURL()+"\"", e);throw new RuntimeException(e);}
		catch (SQLException e) {throw new RuntimeException("Could not get extraction cache.",e);}	
	}

	private TBSLSearch(final SPARQLEndpointEx endpoint,Index resourcesIndex, Index classesIndex, Index propertiesIndex,ExtractionDBCache cache)
	{
		this.endpoint = endpoint;
		try
		{
			// TODO: how can it work everywhere?
			//cacheDir="/tmp/autosparql-cache-tbsl";

			tbsl = new SPARQLTemplateBasedLearner2(endpoint,resourcesIndex,classesIndex,propertiesIndex,POSTaggerHolder.pos,WordNetHolder.wordNet, options,
					cache);
			tbsl.init();
		} catch (Exception e) {throw new RuntimeException(e);}
	}

	public SPARQLEndpointEx getEndpoint() {return endpoint;}

	@Override
	public List<String> getResources(String query) {
		return getResources(query, LIMIT);
	}

	@Override
	public List<String> getResources(String query, int limit) {
		return getResources(query, limit, OFFSET);
	}

	@Override
	public List<String> getResources(String query, int limit, int offset)
	{
		List<String> resources = new ArrayList<String>();

		//tbsl.setEndpoint(endpoint);
		if(!query.startsWith(QUERY_PREFIX)) {query=QUERY_PREFIX+query;}
		tbsl.setQuestion(query);
		try {tbsl.learnSPARQLQueries();}
		catch (NoTemplateFoundException e) {throw new RuntimeException(e);}
		//get SPARQL query which returned result, if exists
		learnedQuery = tbsl.getBestSPARQLQuery();
		return resources;
	}

	@Override
	public SortedSet<Example> getExamples(String query) {
		return getExamples(query, LIMIT, OFFSET);
	}

	@Override
	public SortedSet<Example> getExamples(String query, int limit) {
		return getExamples(query, limit, OFFSET);
	}

	@Override
	public SortedSet<Example> getExamples(String query, int limit, int offset) {
		SortedSet<Example> examples = new TreeSet<Example>();
		log.info("Using TBSLSearch.getExamples() with query \""+query+"\", endpoint \""+endpoint+"\" ...");		
		

		//		try {

		//		} catch (MalformedURLException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}
		if(!query.startsWith(QUERY_PREFIX)) {query=QUERY_PREFIX+query;}
		tbsl.setQuestion(query);
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {throw new RuntimeException(e);}

		//get SPARQL query which returned result, if exists
		learnedQuery  = tbsl.getBestSPARQLQuery();
		if(learnedQuery==null)
		{
			log.info("...unsuccessfully");
			log.warn("No query learned by TBSLSearch with original query: \""+query+"\" at endpoint "+endpoint+". Thus, no examples could be found.");
			return new TreeSet<Example>();
		}
		try
		{
			log.info("Learned Query by TBSL: "+learnedQuery);

			//			learnedQuery = learnedQuery.replace("WHERE {","WHERE {?y ?p1 ?y0. ");
			//			learnedQuery = learnedQuery.replace("SELECT ?y","SELECT distinct *");
			//learnedQuery =  learnedQuery.replace("SELECT ?y","SELECT *");
			ResultSet rs = executeQuery(learnedQuery);
			String uri;
			String lastURI = null;
			Example example = null;
			while(rs.hasNext())
			{
				//TODO: finish
				QuerySolution qs = rs.next();
				uri = qs.get("?y").asResource().getURI();
				if(uri!=lastURI)
				{
					if(example!=null) {examples.add(example);}
					example = new Example();
					example.set("uri", uri);
				}
				//example.set(qs.get("p1").toString(), qs.get("y0").toString());
				lastURI = uri;

				//				Resource resource = qs.getResource(qs.varNames().next());
				//				Example example = new Example();
				//				examples.add(example);
				//				if(resource.isURIResource())
				//				{			
				//					example.set("origin","TBSLSearch");
				//					example.set("uri", resource.getURI());
				//					
				//				}
				//				for(Iterator<String> it = qs.varNames();it.hasNext();)
				//				{
				//					String varName = it.next();
				//					example.set(varName, qs.get(varName).toString());
				//				}
			}
			if(example!=null) {examples.add(example);}
		}
		catch(Exception e)
		{
			log.info("...unsuccessfully");
			e.printStackTrace();
			log.warn("TBSLSearch: Error was thrown by query: "+learnedQuery);
			return new TreeSet<Example>();
		}
		return examples;
	}

	public List<String> getLexicalAnswerType(){
		for(Template t : tbsl.getTemplates()){
			if(t.getLexicalAnswerType() != null){
				return t.getLexicalAnswerType();
			}
		}
		return null;
	}

	protected ResultSet executeQuery(String query)
	{

		QueryEngineHTTP queryExecution = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		for (String dgu : endpoint.getDefaultGraphURIs()) {
			queryExecution.addDefaultGraph(dgu);
		}
		for (String ngu : endpoint.getNamedGraphURIs()) {
			queryExecution.addNamedGraph(ngu);
		}			
		ResultSet resultSet = queryExecution.execSelect();
		return resultSet;
	}

}