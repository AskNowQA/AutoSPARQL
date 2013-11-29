package org.aksw.autosparql.algorithm.tbsl.util;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class RemoteKnowledgebase extends Knowledgebase{
	
	private SparqlEndpoint endpoint;

	public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Indices indices)
	{
		super(label, description, indices);
		this.endpoint = endpoint;
	}
	
	public RemoteKnowledgebase(SparqlEndpoint endpoint, Indices indices)
	{
		super("","",indices);
		this.endpoint = endpoint;
	}
	
//	@Deprecated public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index propertyIndex,
//			Index classIndex, MappingBasedIndex mappingIndex) {
//		super(label, description, resourceIndex, propertyIndex,propertyIndex, classIndex, mappingIndex);
//		this.endpoint = endpoint;
//	}
	public SparqlEndpoint getEndpoint() {return endpoint;}
}