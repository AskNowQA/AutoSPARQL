package org.autosparql.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.autosparql.client.AutoSPARQLService;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.server.util.Endpoints;
import org.autosparql.server.util.SameAsLinks;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AutoSPARQLServiceImpl extends RemoteServiceServlet implements AutoSPARQLService
{
	private static final Logger logger = Logger.getLogger(AutoSPARQLServiceImpl.class);
	private static final long serialVersionUID = 1;	

	enum SessionAttributes{AUTOSPARQL_SESSION}

	private Map<Endpoint, SPARQLEndpointEx> endpointsMap;

	/** The constructor should only be called by org.apache.catalina.core.DefaultInstanceManager.*/
	@Deprecated public AutoSPARQLServiceImpl() {}
	private Set<String> questionWords = new HashSet<String>();

	AutoSPARQLSession session = null; // for testing, gets overwritten in init()	

	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
//		String cacheDir = null;
//		//		try
//		//		{		
//		cacheDir=getServletContext().getRealPath("cache");
//		ExtractionDBCacheUtils.setCacheDir(cacheDir);
//		logger.info("cacheDir for extractiondbcache: "+cacheDir);

		//	loadEndpoints();
		//Test
//		logger.info("Start testing AutoSPARQLServiceImpl...");
//		String cacheDir=getServletContext().getRealPath("cache");
//
//		logger.info("CacheDir: " + cacheDir);
//
//		new ExtractionDBCache(cacheDir).executeSelectQuery(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "SELECT * WHERE {?s ?p ?o.} LIMIT 1");
//
//		
//		logger.info("... finished testing AutoSPARQLServiceImpl.");
		session = createAutoSPARQLSession();
	}

	private void loadEndpoints() throws ConfigurationException
	{
		List<SPARQLEndpointEx> endpoints = Endpoints.loadEndpoints(this.getClass().getClassLoader().getResourceAsStream("/endpoints.xml"));
		endpointsMap = new HashMap<Endpoint, SPARQLEndpointEx>();

		for (SPARQLEndpointEx endpoint : endpoints) {
			endpointsMap.put(new Endpoint(endpoint.getURL().toString(), endpoint.getLabel()), endpoint);
		}
	}

	@Override
	public List<Endpoint> getEndpoints()
	{
		return new ArrayList<Endpoint>(endpointsMap.keySet());
	}

	/** @param query an english query, for example "Give me all books written by Dan Brown".
	 * @return a set of examples which should fit the query
	 **/
	@Override public SortedSet<Example> getExamples(String query)
	{
		String[] tokens = query.split("\\s");
		questionWords.clear();
		for(String token: tokens) {questionWords.add(token);}
		//try {
		SortedSet<Example> examples = null;
		try {
			AutoSPARQLSession session = getAutoSPARQLSession();
			logger.info("Server: AutoSPARQLServiceImpl: Getting examples from AutoSPARQLSession...");
			examples = session.getExamples(query);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		return examples;
		//	}
		//catch (Exception e) {e.printStackTrace();}
		//	return null;
	}

	//	private HttpSession getHttpSession(){
	//		return getThreadLocalRequest().getSession();
	//	}

	private AutoSPARQLSession createAutoSPARQLSession(/*SPARQLEndpointEx endpoint*/)
	{	

		//		}
		//		catch(Throwable t) {logger.error("Error getting servlet context",t); cacheDir="cache";}

		AutoSPARQLSession session = new AutoSPARQLSession(); 
		//getHttpSession().setAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString(), session);
		return session;
	}

	public AutoSPARQLSession getAutoSPARQLSession()
	{		
		//AutoSPARQLSession session = (AutoSPARQLSession) getHttpSession().getAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString());
		return session;	
	}

	@Override
	public SortedSet<Example> getExamplesByQTL(List<String> positives,List<String> negatives)
	{
		return getAutoSPARQLSession().getExamplesByQTL(positives, negatives,questionWords);
	}

	public static void main(String[] args) throws InvalidFileFormatException, FileNotFoundException, IOException, NoTemplateFoundException
	{
		SPARQLTemplateBasedLearner l = new SPARQLTemplateBasedLearner(new Options(TBSLSearch.class.getResourceAsStream("tbsl.properties")));
		l.setQuestion("Give me all books written by Dan Brown");
		l.learnSPARQLQueries();
	}

	@Override
	public Map<String, String> getProperties(String query) throws AutoSPARQLException {
		//logger.debug("Loading properties (" + getSession().getId() + ")");
		return getAutoSPARQLSession().getProperties(query);
	}


	@Override
	public void setFastSearch(Boolean fastSearch)
	{
		getAutoSPARQLSession().setFastSearch(fastSearch);	
	}

	//	@Override
	//	public void setUseDBpediaLive(Boolean useDBpediaLive)
	//	{
	//		getAutoSPARQLSession().setUseDBpediaLive(useDBpediaLive);	
	//	}

	@Override
	public List<String> getSameAsLinks(String resourceURI)
	{
		return SameAsLinks.getSameAsLinksForShowing(resourceURI);
	}

	//@Override public Integer runningClients() {return ++runningClients;}

	/**	hitsAndRunningClients()[0] - hits, [1] - number of running clients */
	// Java does not have pairs or tuples :-(
	@Override public long[] hitsAndNumberOfRunningClients()
	{return new long[] {ActiveClientsListener.hits(),ActiveClientsListener.numberOfConnectedClients()};}

	@Override
	public void setOxford(boolean oxford) {getAutoSPARQLSession().setOxford(oxford);}
}