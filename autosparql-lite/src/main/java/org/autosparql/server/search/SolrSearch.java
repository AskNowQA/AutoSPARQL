package org.autosparql.server.search;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.autosparql.server.Defaults;
import org.autosparql.shared.Example;

public class SolrSearch implements Search
{
	private static Logger logger = Logger.getLogger(SolrSearch.class);

	protected static final int LIMIT = 10;
	protected static final int OFFSET = 0;

	//protected static final String SOLR_DBPEDIA_CLASSES = "http://dbpedia.aksw.org:8080/solr/dbpedia_classes";

	protected CommonsHttpSolrServer server;

	private SolrSearch(String serverURL) // change to public if the need for custom sol server url arises
	{
		try {
			server = new CommonsHttpSolrServer(serverURL);
			server.setRequestWriter(new BinaryRequestWriter());
		} catch (MalformedURLException e) {throw new RuntimeException("Malformed SOLR Server URL \""+serverURL+"\"",e);}
	}

	public SolrSearch() {this(Defaults.solrServerURL());}

	@Override public List<String> getResources(String query) {return getResources(query, LIMIT);	}

	@Override public List<String> getResources(String query, int limit) {return getResources(query, limit, OFFSET);}

	@Override public List<String> getResources(String query, int limit, int offset)
	{
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
		} catch (SolrServerException e) {throw new RuntimeException(e);} // cannot throw because of the Search Interface
		return resources;
	}

	public List<String> getResources(String query, String type) {return getResources(query, type, LIMIT, OFFSET);}
	public List<String> getResources(String query, String type, int limit) {return getResources(query, type, limit, OFFSET);}

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
		} catch (SolrServerException e) {throw new RuntimeException("SOLR Server Exception for query \""+query+"\" and type \""+type+"\"",e);}
		return resources;
	}

	@Override public SortedSet<Example> getExamples(String query) {return getExamples(query, LIMIT, OFFSET);	}
	@Override public SortedSet<Example> getExamples(String query, int limit) {return getExamples(query, limit, OFFSET);}
	@Override public SortedSet<Example> getExamples(String query, int limit, int offset) {	return getExamples(query,null,limit,offset);}

	public SortedSet<Example> getExamples(String query, String type) {return getExamples(query, type, LIMIT, OFFSET);}
	public SortedSet<Example> getExamples(String query, String type, int limit) {return getExamples(query, type, limit, OFFSET);}

	public SortedSet<Example> getExamples(String query, String type, int limit, int offset)
	{
		SortedSet<Example> examples = new TreeSet<Example>();
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
				//example.set("origin","SolrSearch");
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
				return new TreeSet<Example>();
			}
		} catch (SolrServerException e)
		{
			throw new RuntimeException("Error getting examples for query \""+query+"\"",e);
			//			logger.error("SolrSearch.getExamples() with query "+query+" yielded the following exception:");
			//			logger.error("ERROR in SOLRSearch", e);
			//			return new TreeSet<Example>();
		}
		return examples;
	}

	public List<String> getTypes(String term){
		List<String> types = new ArrayList<String>();
		try {
			CommonsHttpSolrServer server = new CommonsHttpSolrServer(TBSLSearch.SOLR_DBPEDIA_CLASSES);
			server.setRequestWriter(new BinaryRequestWriter());
			SolrQuery q = new SolrQuery("label:" + term);
			QueryResponse response = server.query(q);
			SolrDocumentList docList = response.getResults();
			for(SolrDocument d : docList){
				types.add((String) d.get("uri"));
			}
		} catch (Exception e) {throw new RuntimeException(e);}

		return types;
	}

	protected String buildQueryString(String query) {return "comment:(" + query + ") AND NOT label:(" + query + ")";}
	protected String buildQueryString(String query, String type){return "comment:(" + query + ") AND NOT label:(" + query + ") AND types:\"" + type + "\"";}
}