package org.autosparql.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import org.autosparql.client.AutoSPARQLService;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.search.TBSLSearch;
import org.autosparql.server.util.Endpoints;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AutoSPARQLServiceImpl extends RemoteServiceServlet implements AutoSPARQLService
{
	private static final Logger logger = Logger.getLogger(AutoSPARQLServiceImpl.class);
	private static final long serialVersionUID = 1;
	static int runningClients=0;

	enum SessionAttributes{AUTOSPARQL_SESSION}

	private Map<Endpoint, SPARQLEndpointEx> endpointsMap;

	public AutoSPARQLServiceImpl() {}
	private Set<String> questionWords = new HashSet<String>();

	AutoSPARQLSession session;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		//	loadEndpoints();
		//Test
		logger.info("Start testing AutoSPARQLServiceImpl...");
		String cacheDir = null;
		try{cacheDir=getServletContext().getRealPath("cache");}
		catch(Throwable t) {cacheDir="cache";}
		logger.info("CacheDir: " + cacheDir);
		try {
			new ExtractionDBCache(cacheDir).executeSelectQuery(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "SELECT * WHERE {?s ?p ?o.} LIMIT 1");
		} catch (Exception e) {
			logger.error(e);
		}
		logger.info("... finished testing AutoSPARQLServiceImpl.");
		session = createAutoSPARQLSession();
	}

	private void loadEndpoints() {
		try {
			List<SPARQLEndpointEx> endpoints = Endpoints.loadEndpoints(getServletContext().getResource(
					"/WEB-INF/classes/endpoints.xml").getPath());
			endpointsMap = new HashMap<Endpoint, SPARQLEndpointEx>();

			for (SPARQLEndpointEx endpoint : endpoints) {
				endpointsMap.put(new Endpoint(endpoint.getURL().toString(), endpoint.getLabel()), endpoint);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Endpoint> getEndpoints()
	{
		return new ArrayList<Endpoint>(endpointsMap.keySet());
	}

	@Override
	public SortedSet<Example> getExamples(String query)
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
		String cacheDir = null;
		try{cacheDir=getServletContext().getRealPath("cache");}
		catch(Throwable t) {logger.error("Error getting servlet context",t); cacheDir="cache";}

		AutoSPARQLSession session = new AutoSPARQLSession(cacheDir); 
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

	@Override
	public void setUseDBpediaLive(Boolean useDBpediaLive)
	{
		getAutoSPARQLSession().setUseDBpediaLive(useDBpediaLive);	
	}

	@Override
	public List<String> getSameAsLinks(String resourceURI)
	{
		return AutoSPARQLSession.getSameAsLinks(resourceURI);
	}

	@Override public Integer runningClients() {return ++runningClients;}
}