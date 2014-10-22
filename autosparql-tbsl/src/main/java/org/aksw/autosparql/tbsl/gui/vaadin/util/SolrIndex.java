package org.aksw.autosparql.tbsl.gui.vaadin.util;

import java.util.ArrayList;
import java.util.List;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrIndex implements FallbackIndex{

	private HttpSolrServer server;

	public SolrIndex(String solrServerURL)
	{
			server = new HttpSolrServer(solrServerURL);
			server.setRequestWriter(new BinaryRequestWriter());
	}

	public List<BasicResultItem> getData(String queryString, int limit, int offset) {
		List<BasicResultItem> items = new ArrayList<BasicResultItem>();
		QueryResponse response;
		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", queryString);
			params.set("rows", limit);
			params.set("start", offset);
//			params.set("sort", "score+desc,pagerank+desc");
			response = server.query(params);
			SolrDocumentList docList = response.getResults();
			BasicResultItem example;
			for(SolrDocument d : docList){
				example = new BasicResultItem((String) d.get("uri"), (String) d.get("label"),(String) d.get("comment"),
						(String) d.get("imageURL"));
				items.add(example);
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		return items;
	}


}
