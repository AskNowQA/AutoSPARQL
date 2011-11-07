package org.autosparql.client;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("AutoSPARQLService")
public interface AutoSPARQLService extends RemoteService {
	/**
	 * Utility class for simplifying access to the instance of async service.
	 */
	public static class Util {
		private static AutoSPARQLServiceAsync instance;
		public static AutoSPARQLServiceAsync getInstance(){
			if (instance == null) {
				instance = GWT.create(AutoSPARQLService.class);
			}
			return instance;
		}
	}
	
	List<Endpoint> getEndpoints();
	
	SortedSet<Example> getExamples(String query);

	Map<String, String> getProperties(String query) throws AutoSPARQLException;

	SortedSet<Example> getExamplesByQTL(List<String> positives,List<String> negatives);
}