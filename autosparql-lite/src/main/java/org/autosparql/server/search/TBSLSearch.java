package org.autosparql.server.search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.sparql.Template;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class TBSLSearch implements Search
{
	private static Logger logger = Logger.getLogger(TBSLSearch.class);
	private static final String OPTIONS_FILE = "org/autosparql/server/tbsl.properties";

	private static final int LIMIT = 10;
	private static final int OFFSET = 0;

	private static final String QUERY_PREFIX = "Give me all ";

	private SPARQLTemplateBasedLearner tbsl;
	private SparqlEndpoint endpoint;

	public TBSLSearch(SparqlEndpoint endpoint){
		this.endpoint = endpoint;
		try
		{
			tbsl = new SPARQLTemplateBasedLearner(new Options(getClass().getClassLoader().getResource(OPTIONS_FILE)), new ApachePartOfSpeechTagger());
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

		tbsl.setEndpoint(endpoint);
		if(!query.startsWith(QUERY_PREFIX)) {query=QUERY_PREFIX+query;}
		tbsl.setQuestion(query);
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		//get SPARQL query which returned result, if exists
		String learnedQuery = tbsl.getBestSPARQLQuery();


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
		logger.info("Using TBSLSearch.getExamples() with query \""+query+"\"...");
		tbsl.setEndpoint(endpoint);
		if(!query.startsWith(QUERY_PREFIX)) {query=QUERY_PREFIX+query;}
		tbsl.setQuestion(query);
		try {
			tbsl.learnSPARQLQueries();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		//get SPARQL query which returned result, if exists
		String learnedQuery = tbsl.getBestSPARQLQuery();
		if(learnedQuery==null)
		{
			logger.info("...unsuccessfully");
			logger.warn("No query learned by TBSLSearch with original query: \""+query+"\" at endpoint "+endpoint+". Thus, no examples could be found.");
			return new TreeSet<Example>();
		}
		try
		{
			logger.info("Learned Query by TBSL: "+learnedQuery);
			
//			learnedQuery = learnedQuery.replace("WHERE {","WHERE {?y ?p1 ?y0. ");
//			learnedQuery = learnedQuery.replace("SELECT ?y","SELECT distinct *");
			//learnedQuery =  learnedQuery.replace("SELECT ?y","SELECT *");
			System.out.println(learnedQuery);
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
			logger.info("...unsuccessfully");
			e.printStackTrace();
			logger.warn("TBSLSearch: Error was thrown by query: "+learnedQuery);
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

	private ResultSet executeQuery(String query)
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
