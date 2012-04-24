package org.autosparql.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.search.SolrSearch;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.server.util.DefaultPrefixMapping;
import org.autosparql.server.util.LanguageResolver;
import org.autosparql.shared.BlackList;
import org.autosparql.shared.Example;
import org.autosparql.shared.SPARQLException;
import org.autosparql.shared.SameAsWhiteList;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.openjena.atlas.logging.Log;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

/** @author Konrad Höffner */
public class AutoSPARQLSession
{
	private static Logger logger = Logger.getLogger(AutoSPARQLSession.class);
	protected Map<String, String> property2LabelMap;
	protected TBSLSearch primarySearch;
	protected SolrSearch secondarySearch;

	protected final String cacheDir = "cache";
	protected SPARQLEndpointEx endpoint;
	protected final ExtractionDBCache selectCache;

	public static final List<String> languages = Arrays.asList(new String[] {"de","en"});
	protected static final int MAX_NUMBER_OF_EXAMPLES = 20;
	String lastQuery = null;
	PrefixMapping x = new PrefixMappingImpl();
	protected boolean fastSearch = false;

	protected static final String sameAsURI = "http://sameas.org/rdf?uri=";

	/** Using this instead of getCacheManager() allows us to safely use CacheManager.shutdown()
	 * after each method dealing with the cache,
	 * thus preventing corruptions of the cache (which Ehcache is *really* susceptible to, even if no write or even read is written)*/
	public static CacheManager getCacheManager()
	{
		CacheManager cm = CacheManager.getInstance();
		if(cm==null) {cm=CacheManager.create();}
		return cm;
	}
	
	// public AutoSPARQLSession(SPARQLEndpointEx endpoint, String cacheDir,
	// String servletContextPath, String solrURL, QuestionProcessor
	// questionPreprocessor){
	// this.endpoint = endpoint;
	// this.servletContextPath = servletContextPath;
	// this.questionPreprocessor = questionPreprocessor;
	//
	// constructCache = new ExtractionDBCache(cacheDir + "/" +
	// endpoint.getPrefix() + "/construct-cache");

	// search = new SolrSearch(solrURL, questionPreprocessor);
	// exampleFinder = new ExampleFinder(endpoint, selectCache, constructCache,
	// search, questionPreprocessor);
	//
	// property2LabelMap = new TreeMap<String, String>();
	// property2DatatypeMap = new HashMap<String, Class>();
	// propertiesCache = new TreeMap<String, Map<String,Object>>();
	// examplesCache = new HashMap<String, Example>();
	// }

	public void setUseDBpediaLive(boolean useDBpediaLive)
	{
		try
		{
			if(useDBpediaLive)
			{
				logger.info("setting endpoint to DBpedia live");
				this.endpoint = new SPARQLEndpointEx(new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql")),"dbpedialive",null,Collections.<String>emptySet());
			} else
			{
				logger.info("setting endpoint to DBpedia");
				this.endpoint = new SPARQLEndpointEx(new SparqlEndpoint(new URL("http://dbpedia.org/sparql")),"dbpedia",null,Collections.<String>emptySet());
			}
		} catch (MalformedURLException e){throw new RuntimeException(e);}
	}

	public void setFastSearch(boolean fastSearch)
	{
		this.fastSearch = fastSearch;
		logger.info("setting fast search to "+fastSearch);
	}

	public AutoSPARQLSession(SparqlEndpoint endpoint, String solrServerURL, String cacheDir)
	{
		this.endpoint= new SPARQLEndpointEx(endpoint,endpoint.toString(),null,Collections.<String>emptySet());
		String dir = cacheDir;
		try {
			dir = cacheDir + "/" + URLEncoder.encode(this.endpoint.getURL().toString(), "UTF-8")+ "/select-cache";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		selectCache = new ExtractionDBCache(dir);
		try {
			selectCache.executeSelectQuery(endpoint, "SELECT * WHERE {?s a ?type} LIMIT 1");
		} catch (Exception e) {
			logger.error("ERROR", e);
			e.printStackTrace();
		}

		primarySearch = new TBSLSearch(endpoint, cacheDir);
		secondarySearch = new SolrSearch(solrServerURL);
	}

	/** learns new examples and processes them by removing blacklisted properties and choosing the ideal language in case there are multiple candidates 
	 * with different language tags for the same URI and property*/
	public SortedSet<Example> getExamplesByQTL(List<String> positives,List<String> negatives,Set<String> questionWords)
	{
		logger.info("getExamplesByQTL("+positives+","+negatives+","+questionWords+")");
		//		Cache cache = getCacheManager().getCache("qtl");
		//		List<Collection> parameters  = new LinkedList<Collection>(Arrays.asList(new Collection[]{positives,negatives,questionWords}));
		//		{
		//			Element e;
		//			if((e=cache.get(parameters))!=null) {return (SortedSet<Example>)e.getValue();}
		//		}
		QTL qtl = new QTL(endpoint, selectCache);
		qtl.setExamples(positives, negatives);
		if(questionWords!=null) {qtl.addStatementFilter(new QuestionBasedStatementFilter(questionWords));}
		qtl.start();
		// TODO extract relevant words
		// behält nur die kanten wo die property mit einem wort oder das objekt ähnlichkeit hat		
		String query = qtl.getBestSPARQLQuery();
		// get all triples belonging to the subjects
		query = query.replace("SELECT ?x0 WHERE {", "SELECT ?x0 ?p ?o WHERE {?x0 ?p ?o. ");
		query = query.replace("?x0", "?s");

		try
		{
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, query));
			SortedSet<Example> examples = fillExamples(null, rs);
			//			cache.put(new Element(parameters,examples));
			//			cache.flush();
			if(examples.size()>MAX_NUMBER_OF_EXAMPLES) {return new TreeSet<Example>(new LinkedList<Example>(examples).subList(0, MAX_NUMBER_OF_EXAMPLES-1));}
			return examples;


			//			Map<String,Example> examples = new HashMap<String,Example>();
			//			LanguageResolver resolver = new LanguageResolver();
			//			for(QuerySolution qs=rs.next(); rs.hasNext();qs=rs.next())
			//			{
			//				String property = qs.getResource("p").getURI();
			//				if(BlackList.dbpedia.contains(property)) {continue;}
			//				String uri = qs.getResource("x0").getURI();
			//
			//				Example e=examples.containsKey(uri)?examples.get(uri):new Example(uri);
			//				examples.put(uri,e);
			//
			//				String object = qs.get("o").toString();
			//
			//				String oldObject=e.get(property);
			//				if(oldObject!=null)
			//				{
			//					e.set(property, resolver.resolve(oldObject, object));
			//				}
			//
			//				e.set(property,object.toString());
			//			}
			//			return new ArrayList<Example>(examples.values());
		}
		catch(Exception e)
		{
			throw new SPARQLException(e,query,endpoint.toString());
		}
	}

	public List<String> getResources(String query)
	{
		List<String> resources = new ArrayList<String>();
		// primary search DBpedia bzw. DBpedia live
		resources = primarySearch.getResources(query);
		if (resources.isEmpty())
		{
			// fallback: string in solr index hauen und zurückgeben was da
			// rauskommt
			List<String> answerType = primarySearch.getLexicalAnswerType();
			List<String> types = secondarySearch.getTypes(answerType.get(0));

			for (String type : types)
			{
				resources = secondarySearch.getResources(query, type);
				if (!resources.isEmpty())
				{
					return resources;
				}
			}
		}

		return resources;
	}


	/** @param examples the list of existing examples. if null a new one will be created. examples not contained will be created.
	/** @param rs a resultset whose variables have to be "s", "p" and "o"
	/** @return the original list if non-null (else a new one) filled with the properties and one object per property from the resultset.*/
	public static SortedSet<Example> fillExamples(SortedSet<Example> examples, ResultSet rs)
	{
		if(examples==null) {examples = new TreeSet<Example>();}
		if(!rs.hasNext()) {return new TreeSet<Example>(examples);}

		LanguageResolver resolver = new LanguageResolver(new String[] {"en","de","es"});
		Map<String,Example> uriToExample = new HashMap<String,Example>();
		for(Example example: examples) {uriToExample.put(example.getURI(), example);}
		for(QuerySolution qs=rs.next();rs.hasNext();qs=rs.next())
		{
			String property = qs.getResource("p").getURI();

			if(BlackList.dbpedia.contains(property)) {continue;}
			// TODO: extend blacklist with regular expressions so that this can be made through the blacklist
			if(property.startsWith("http://dbpedia.org/property/") || property.startsWith("http://dbpedia.org/resource/")) {continue;}
			String uri = qs.getResource("?s").getURI();

			Example e=uriToExample.containsKey(uri)?uriToExample.get(uri):new Example(uri);
			uriToExample.put(uri,e);

			RDFNode object = qs.get("o");
			String objectString = object.toString();
			if(object.isURIResource()){
				try{objectString = URLDecoder.decode(objectString,"UTF-8");} catch (UnsupportedEncodingException e1){throw new RuntimeException(e1);}
			}

			String oldObject=e.get(property);
			if(oldObject!=null)
			{
				e.set(property, resolver.resolve(oldObject, objectString));
			}
			else
			{
				e.set(property,objectString);
			}
		}
		examples.addAll(uriToExample.values());

		return examples;
	}

	/** Adds all existing properties for the uris in the examples and one object for each one (depending on the languages)
	 * @param examples */
	public void fillExamples(SortedSet<Example> examples)
	{		
		if(examples==null) {examples= new TreeSet<Example>();}
		if(examples.isEmpty()) {System.err.println("Examples are empty.");return;}
		List<String> uris = new LinkedList<String>();
		for(Example example: examples)
		{
			//	System.out.println("TEST: " + example);
			uris.add(example.getURI());
			example.setSameAsLinks(getSameAsLinks(example.getURI()));
		}
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * from <http://dbpedia.org> { ?s ?p ?o. FILTER(");
		for(String uri:uris) {sb.append("?s = <"+DefaultPrefixMapping.INSTANCE.expandPrefix(uri)+">||");}
		// remove last "||"-substring
		String query = sb.substring(0,sb.length()-2)+")}";
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, query));
		fillExamples(examples,rs);
	}

	public static SortedSet<Example> mapsToExamples(List<Map<String,Object>> maps)
	{
		SortedSet<Example> examples = new TreeSet<Example>();
		for(Map<String,Object> map: maps)
		{
			Example example = new Example();
			for(String property : map.keySet()) {example.set(property,map.get(property).toString());}
			//example.setProperties(map);
			example.setSameAsLinks((List<String>) map.get(OWL.sameAs.getURI()));
			examples.add(example);

		}
		return examples;
	}

	public static List<Map<String,Object>> examplesToMaps(Collection<Example> examples)
	{
		LinkedList<Map<String,Object>> maps = new LinkedList<Map<String,Object>>();
		for(Example example : examples)
		{
			// this line gives errors with gxt:
			// maps.add(example.getProperties());
			// thus do it with individual toString to prevent gxt objects from sneaking in
			Map<String,Object> map = new HashMap<String,Object>();
			for(String property : example.getProperties().keySet())
			{
				map.put(property,example.get(property).toString());
			}
			if(example.getSameAsLinks()!=null&&!example.getSameAsLinks().isEmpty())
			{map.put(OWL.sameAs.getURI(), example.getSameAsLinks());}
			maps.add(map);
		}
		return maps;
	}

	public static String cacheKey(String query, boolean fastSearch)
	{
		return query+"+fastsearch="+(fastSearch?"on":"off");
	}

	@SuppressWarnings("unchecked")
	public SortedSet<Example> getExamples(String query)
	{
		Cache cache = getCacheManager().getCache("examples");
		{try{
			Element e=cache.get(cacheKey(query,fastSearch));
			//System.out.println(e);
			if(e!=null) {
				logger.info("cache hit with query \""+query+"\"");
				getCacheManager().shutdown();
				return mapsToExamples((List<Map<String,Object>>)e.getValue());}
			else{logger.info("cache miss with query \""+query+"\"");}
		}	catch(Exception e) {System.err.println("Error getting cache element.");e.printStackTrace();}
		}
		SortedSet<Example> examples = null;// = new ArrayList<Example>();
		//		 primary search DBpedia bzw. DBpedia live
		if(!fastSearch) {examples = primarySearch.getExamples(query);}
		if(examples==null||examples.isEmpty())
		{
			//		//			 fallback: string in solr index hauen und zurückgeben was da
			//		//			 rauskommt
			//					List<String> answerType = primarySearch.getLexicalAnswerType();
			//					if(answerType==null)
			//					{
			examples = secondarySearch.getExamples(query);
			lastQuery=query;
		}
		//			else
		//			{
		//				List<String> types = secondarySearch.getTypes(answerType.get(0));
		//				for (String type : types)
		//				{
		//					examples = secondarySearch.getExamples(query, type);
		//					if (!examples.isEmpty())
		//					{
		//						return examples;
		//					}
		//				}
		//			}
		//		}
		fillExamples(examples);
		if(examples.isEmpty()) {logger.warn("AutoSPARQLSession found no examples for query \""+query+"\". :-(");}
		cache.put(new Element(cacheKey(query,fastSearch),examplesToMaps(examples)));
		cache.flush();
		getCacheManager().shutdown();
		return examples;
	}

	public Map<String, String> getProperties(String query) throws AutoSPARQLException
	{
		property2LabelMap = new TreeMap<String, String>();

		String queryTriples = query.substring(18, query.length() - 1);

		String newQuery = "SELECT DISTINCT ?p ?label WHERE {" + queryTriples
				+ "?x0 ?p ?o. " + "?p <" + RDFS.label
				+ "> ?label. FILTER(LANGMATCHES(LANG(?label), 'en'))} "
				+ "LIMIT 1000";

		ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache
				.executeSelectQuery(endpoint, newQuery));
		QuerySolution qs;
		while (rs.hasNext())
		{
			qs = rs.next();
			property2LabelMap.put(qs.getResource("p").getURI(),
					qs.getLiteral("label").getLexicalForm());
		}

		Iterator<String> it = property2LabelMap.keySet().iterator();
		while (it.hasNext())
		{
			String uri = it.next();
			if (!uri.startsWith("http://dbpedia.org/ontology"))
			{
				it.remove();
			}
		}
		property2LabelMap.put(RDFS.label.getURI(), "label");

		return property2LabelMap;
	}

	public static List<String> getSameAsLinks(String resourceURI) {
		List<String> sameAsLinks = new ArrayList<String>();
		try {
			String requestURI = sameAsURI + URLEncoder.encode(resourceURI, "UTF-8");
			URLConnection conn = new URL(requestURI).openConnection();
			Model model = ModelFactory.createDefaultModel();
			model.read(conn.getInputStream(), null);
			String url;
			Set<String> used = new HashSet<String>();
			for(Statement st : model.listStatements(null, OWL.sameAs, (RDFNode)null).toList()){
				url = st.getObject().asResource().getURI();
				String prefix;
				if((prefix = SameAsWhiteList.isAllowed(url)) != null && used.add(prefix)){
					sameAsLinks.add(url);
				}
			}
			sameAsLinks.add(resourceURI.replace("http://dbpedia.org/resource/", "http://en.wikipedia.org/wiki/"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sameAsLinks;
	}
}