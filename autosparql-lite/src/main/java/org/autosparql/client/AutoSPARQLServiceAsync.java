package org.autosparql.client;

import java.util.List;
import java.util.Map;

import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AutoSPARQLServiceAsync
{
	void getEndpoints(AsyncCallback<List<Endpoint>> callback);

	void getExamples(String query, AsyncCallback<List<Example>> callback);
	void getProperties(String query,AsyncCallback<Map<String, String>> callback);

}
