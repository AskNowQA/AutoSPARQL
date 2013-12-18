package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
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
	
		@Deprecated public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index propertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
			super(description, description, null);
			throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");
	}
	public SparqlEndpoint getEndpoint() {return endpoint;}
}