package org.autosparql.server.search;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.autosparql.shared.Example;

public class SolrSearch implements Search
{
	private static Logger logger = Logger.getLogger(SolrSearch.class);

	private static final int LIMIT = 10;
	private static final int OFFSET = 0;

	private CommonsHttpSolrServer server;

	public SolrSearch(String serverURL){
		try {
			server = new CommonsHttpSolrServer(serverURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {
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
	public List<String> getResources(String query, int limit, int offset) {
		List<String> resources = new ArrayList<String>();

		SolrQuery q = new SolrQuery(buildQueryString(query));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				resources.add((String) d.get("uri"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}

	public List<String> getResources(String query, String type) {
		return getResources(query, type, LIMIT, OFFSET);
	}

	public List<String> getResources(String query, String type, int limit) {
		return getResources(query, type, limit, OFFSET);
	}

	public List<String> getResources(String query, String type, int limit, int offset) {
		List<String> resources = new ArrayList<String>();

		SolrQuery q = new SolrQuery(buildQueryString(query, type));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				resources.add((String) d.get("uri"));
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return resources;
	}

	@Override
	public List<Example> getExamples(String query) {
		return getExamples(query, LIMIT, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit) {
		return getExamples(query, limit, OFFSET);
	}

	@Override
	public List<Example> getExamples(String query, int limit, int offset)
	{
		return getExamples(query,null,limit,offset);
	}

	public List<Example> getExamples(String query, String type) {
		return getExamples(query, type, LIMIT, OFFSET);
	}

	public List<Example> getExamples(String query, String type, int limit) {
		return getExamples(query, type, limit, OFFSET);
	}

	public List<Example> getExamples(String query, String type, int limit, int offset)
	{
		List<Example> examples = new ArrayList<Example>();
		logger.info("Using SolrSearch.getExamples()");
		SolrQuery q = (type==null)?new SolrQuery(buildQueryString(query)):new SolrQuery(buildQueryString(query, type));
		q.setRows(limit);
		q.setStart(offset);
		try {
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			String uri;
			String label;
			String imageURL;
			String comment;
			for(SolrDocument d : docList)
			{
				uri = (String) d.get("uri");
				label = (String) d.get("label");
				imageURL = (String) d.get("imageURL").toString();
				comment = (String) d.get("comment");
				Example example = new Example(uri, label, imageURL, comment);
				example.set("origin","SolrSearch");
				logger.trace("SolrSearch Field Value Map:"+d.getFieldValueMap());
//				for(String property: d.getFieldNames())
//				{
//					example.set(property, d.getFieldValue(property));
//				}
				examples.add(example);
			}
			if(examples.isEmpty())
			{
				logger.warn("No query learned by SolrSearch with original query: "+query);
				return Collections.<Example>emptyList();
			}
		} catch (SolrServerException e) {
			logger.error("SolrSearch.getExamples() with query "+query+"yielded the following exception:");
			logger.error(e);
			logger.error(Arrays.toString(e.getStackTrace()));
			return Collections.<Example>emptyList();
		}
		return examples;
	}

	public List<String> getTypes(String term){
		List<String> types = new ArrayList<String>();
		try {
			CommonsHttpSolrServer server = new CommonsHttpSolrServer("http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_classes");
			server.setRequestWriter(new BinaryRequestWriter());
			SolrQuery q = new SolrQuery("label:" + term);
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				types.add((String) d.get("uri"));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return types;
	}

	private String buildQueryString(String query){
		return "comment:(" + query + ") AND NOT label:(" + query + ")";
	}

	private String buildQueryString(String query, String type){
		return "comment:(" + query + ") AND NOT label:(" + query + ") AND types:\"" + type + "\"";
	}

}
