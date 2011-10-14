package org.autosparql.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.search.SolrSearch;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.RDFS;

/** @author Konrad Höffner

 */
public class AutoSPARQLSession
{
	private static Logger logger = Logger.getLogger(AutoSPARQLSession.class);
	private Map<String, String> property2LabelMap;
	private TBSLSearch primarySearch;
	private SolrSearch secondarySearch;

	private SPARQLEndpointEx endpoint;
	private ExtractionDBCache selectCache;

	// public AutoSPARQLSession(SPARQLEndpointEx endpoint, String cacheDir,
	// String servletContextPath, String solrURL, QuestionProcessor
	// questionPreprocessor){
	// this.endpoint = endpoint;
	// this.servletContextPath = servletContextPath;
	// this.questionPreprocessor = questionPreprocessor;
	//
	// constructCache = new ExtractionDBCache(cacheDir + "/" +
	// endpoint.getPrefix() + "/construct-cache");
	// selectCache = new ExtractionDBCache(cacheDir + "/" + endpoint.getPrefix()
	// + "/select-cache");
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
		primarySearch = new TBSLSearch(endpoint);
		secondarySearch = new SolrSearch(solrServerURL);
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
		examples = primarySearch.getExamples(query);
		if (examples.isEmpty())
		{
			// fallback: string in solr index hauen und zurückgeben was da
			// rauskommt
			List<String> answerType = primarySearch.getLexicalAnswerType();
			if(answerType==null)
			{
				examples = secondarySearch.getExamples(query);
			}
			else
			{
				List<String> types = secondarySearch.getTypes(answerType.get(0));
				for (String type : types)
				{
					examples = secondarySearch.getExamples(query, type);
					if (!examples.isEmpty())
					{
						return examples;
					}
				}
			}
		}
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
