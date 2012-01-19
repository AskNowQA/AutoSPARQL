package org.autosparql.client;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AutoSPARQLServiceAsync
{
	void getEndpoints(AsyncCallback<List<Endpoint>> callback);
	void getExamples(String query, AsyncCallback<SortedSet<Example>> callback);
	void getProperties(String query,AsyncCallback<Map<String, String>> callback);

	void getExamplesByQTL(List<String> positives, List<String> negatives,AsyncCallback<SortedSet<Example>> callback);
	void setFastSearch(Boolean fastSearch, AsyncCallback<Void> callback);
	void setUseDBpediaLive(Boolean useDBpediaLive, AsyncCallback<Void> callback);
	
	void getSameAsLinks(String resourceURI, AsyncCallback<List<String>> callback);
}