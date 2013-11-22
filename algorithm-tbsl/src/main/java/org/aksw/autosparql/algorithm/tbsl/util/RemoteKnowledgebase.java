package org.aksw.autosparql.algorithm.tbsl.util;

import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class RemoteKnowledgebase extends Knowledgebase{
	
	private SparqlEndpoint endpoint;

	public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index objectPropertyIndex,Index dataPropertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
		super(label, description, resourceIndex, objectPropertyIndex,dataPropertyIndex, classIndex, mappingIndex);
		this.endpoint = endpoint;
	}

	@Deprecated public RemoteKnowledgebase(SparqlEndpoint endpoint, String label, String description, Index resourceIndex, Index propertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
		super(label, description, resourceIndex, propertyIndex,propertyIndex, classIndex, mappingIndex);
		this.endpoint = endpoint;
	}

	public SparqlEndpoint getEndpoint() {
		return endpoint;
	}
	

}
