package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class RemoteKnowledgebase extends Knowledgebase {
	private final ExtractionDBCache cache;
	private final SparqlEndpoint endpoint;

	public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Indices indices)
	{
		super(label, description, indices);
		this.endpoint = endpoint;
		cache = new ExtractionDBCache(label);
		cache.setMaxExecutionTimeInSeconds(60);
	}
	
	public RemoteKnowledgebase(SparqlEndpoint endpoint, Indices indices) {this(endpoint,"","",indices);}
	
		@Deprecated public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index propertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
			super(description, description, null);
			throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");
	}
	public SparqlEndpoint getEndpoint() {return endpoint;}
		
	@Override public ResultSet querySelect(String query)
	{
		if (cache == null) {
			QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
			qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			return qe.execSelect();
		} else {
			return SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		}
	}
}