package org.autosparql.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.search.SolrSearch;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.server.util.LanguageResolver;
import org.autosparql.shared.BlackList;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDFS;

/** @author Konrad Höffner */
public class AutoSPARQLSession
{
	private static Logger logger = Logger.getLogger(AutoSPARQLSession.class);
	private Map<String, String> property2LabelMap;
	private TBSLSearch primarySearch;
	private SolrSearch secondarySearch;

	private final String cacheDir = "cache";
	private final SPARQLEndpointEx endpoint;
	private final ExtractionDBCache selectCache;

	public static final List<String> languages = Arrays.asList(new String[] {"de","en"});

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

	public AutoSPARQLSession(SparqlEndpoint endpoint, String solrServerURL)
	{
		this.endpoint= new SPARQLEndpointEx(endpoint,endpoint.toString(),null,Collections.<String>emptySet());
		selectCache = new ExtractionDBCache(cacheDir + "/" + this.endpoint.getBaseURI()+ "/select-cache");

		primarySearch = new TBSLSearch(endpoint);
		secondarySearch = new SolrSearch(solrServerURL);
	}


	/** learns new examples and processes them by removing blacklisted properties and choosing the ideal language in case there are multiple candidates 
	 * with different language tags for the same URI and property*/
	public List<Example> getExamplesByQTL(List<String> positives,List<String> negatives,Set<String> questionWords)
	{
		QTL qtl = new QTL(endpoint, selectCache);
		qtl.setExamples(positives, negatives);
		if(questionWords!=null) {qtl.addStatementFilter(new QuestionBasedStatementFilter(questionWords));}
		qtl.start();
		// TODO extract relevant words
		// behält nur die kanten wo die property mit einem wort oder das objekt ähnlichkeit hat		
		String query = qtl.getBestSPARQLQuery();
		// get all triples belonging to the subjects
		query = query.replace("SELECT ?x0 WHERE {", "SELECT ?x0 ?p ?o WHERE {?x0 ?p ?o. ");
		System.out.println(query);
		try
		{
			ResultSet rs = SparqlQuery.convertJSONtoResultSet(selectCache.executeSelectQuery(endpoint, query));
			Map<String,Example> examples = new HashMap<String,Example>();
			LanguageResolver resolver = new LanguageResolver();
			for(QuerySolution qs=rs.next(); rs.hasNext();qs=rs.next())
			{
				String property = qs.getResource("p").getURI();
				if(BlackList.dbpedia.contains(property)) {continue;}
				String uri = qs.getResource("x0").getURI();

				Example e=examples.containsKey(uri)?examples.get(uri):new Example(uri);
				examples.put(uri,e);

				String object = qs.get("o").toString();
		
				String oldObject=e.get(property);
				if(oldObject!=null)
				{
					e.set(property, resolver.resolve(oldObject, object));
				}
				
				e.set(property,object.toString());
			}
			return new ArrayList<Example>(examples.values());
		}
		catch(Exception e)
		{
			throw new RuntimeException("Error with query "+query,e);
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

	public List<Example> getExamples(String query)
	{
		List<Example> examples = new ArrayList<Example>();
		// primary search DBpedia bzw. DBpedia live
		//		examples = primarySearch.getExamples(query);
		//		if (examples.isEmpty())
		//		{
		////			 fallback: string in solr index hauen und zurückgeben was da
		////			 rauskommt
		//			List<String> answerType = primarySearch.getLexicalAnswerType();
		//			if(answerType==null)
		//			{
		examples = secondarySearch.getExamples(query);
		//			}
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
		if(examples.isEmpty()) {logger.warn("AutoSPARQLSession found no examples for query \""+query+"\". :-(");}
		return examples;
	}

	public Map<String, String> getProperties(String query)
			throws AutoSPARQLException
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
}
