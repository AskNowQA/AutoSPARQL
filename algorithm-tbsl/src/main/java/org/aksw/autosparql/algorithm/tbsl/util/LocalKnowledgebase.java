package org.aksw.autosparql.algorithm.tbsl.util;

import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;

import com.hp.hpl.jena.rdf.model.Model;

public class LocalKnowledgebase extends Knowledgebase{
	
	private Model model;
	
	public LocalKnowledgebase(Model model, String label, String description, Index resourceIndex, Index objectPropertyIndex,Index dataPropertyIndex,
			Index classIndex, MappingBasedIndex mappingIndex) {
		super(label, description, resourceIndex, objectPropertyIndex, dataPropertyIndex,classIndex, mappingIndex);
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}

}
