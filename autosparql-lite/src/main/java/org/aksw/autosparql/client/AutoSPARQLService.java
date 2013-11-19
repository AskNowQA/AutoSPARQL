package org.aksw.autosparql.client;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.aksw.autosparql.client.exception.AutoSPARQLException;
import org.aksw.autosparql.shared.Endpoint;
import org.aksw.autosparql.shared.Example;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("AutoSPARQLService")
public interface AutoSPARQLService extends RemoteService
{
	/** Utility class for simplifying access to the instance of async service. */
	public static class Util
	{
		public static final AutoSPARQLServiceAsync instance = GWT.create(AutoSPARQLService.class);
		public static AutoSPARQLServiceAsync getInstance()
		{			
			return instance;
		}
	}	
	List<Endpoint> getEndpoints() throws Exception;	
	SortedSet<Example> getExamples(String query) throws Exception;
	Map<String, String> getProperties(String query) throws AutoSPARQLException;
	SortedSet<Example> getExamplesByQTL(List<String> positives,List<String> negatives);
	void setFastSearch(Boolean fastSearch);
	void setOxford(boolean oxford);
//	void setUseDBpediaLive(Boolean useDBpediaLive);
	List<String> getSameAsLinks(String resourceURI);
	//Integer runningClients();
	long[] hitsAndNumberOfRunningClients();
}