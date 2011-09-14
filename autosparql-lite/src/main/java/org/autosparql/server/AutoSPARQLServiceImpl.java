package org.autosparql.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.client.exception.AutoSPARQLException;
import org.autosparql.server.util.Endpoints;
import org.autosparql.shared.Endpoint;
import org.autosparql.shared.Example;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.ini4j.InvalidFileFormatException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AutoSPARQLServiceImpl extends RemoteServiceServlet implements AutoSPARQLService {
	
	enum SessionAttributes{
		AUTOSPARQL_SESSION
	}
	
	private Map<Endpoint, SPARQLEndpointEx> endpointsMap;
	
	public AutoSPARQLServiceImpl() {
		
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		loadEndpoints();
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
	public List<Endpoint> getEndpoints() {
		return new ArrayList<Endpoint>(endpointsMap.keySet());
	}
	
	@Override
	public List<Example> getExamples(String query) {
		//List<Example> examples = new ArrayList<Example>();
		try {
			AutoSPARQLSession session = getAutoSPARQLSession();
			return session.getExamples(query);
//			for(String resource : session.getResources(query)){
//				examples.add(new Example(resource, "", "", ""));
			}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private HttpSession getHttpSession(){
		return getThreadLocalRequest().getSession();
	}
	
	private AutoSPARQLSession createAutoSPARQLSession(SPARQLEndpointEx endpoint){
		AutoSPARQLSession session = new AutoSPARQLSession(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "http://139.18.2.173:8080/apache-solr-3.3.0/dbpedia_resources");
		getHttpSession().setAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString(), session);
		return session;
	}
	
	private AutoSPARQLSession getAutoSPARQLSession(){
		AutoSPARQLSession session = (AutoSPARQLSession) getHttpSession().getAttribute(SessionAttributes.AUTOSPARQL_SESSION.toString());
		if(session == null){
			session = createAutoSPARQLSession(null);
		}
		return session;
	}
	
	public static void main(String[] args) throws InvalidFileFormatException, FileNotFoundException, IOException, NoTemplateFoundException {
		SPARQLTemplateBasedLearner l = new SPARQLTemplateBasedLearner(AutoSPARQLServiceImpl.class.getClassLoader().getResource("org/autosparql/server/tbsl.properties").getPath());
		l.setQuestion("Give me all books written by Dan Brown");
		l.learnSPARQLQueries();
	}

	@Override
	public Map<String, String> getProperties(String query) throws AutoSPARQLException {
		//logger.debug("Loading properties (" + getSession().getId() + ")");
		return getAutoSPARQLSession().getProperties(query);
	}

	
}
