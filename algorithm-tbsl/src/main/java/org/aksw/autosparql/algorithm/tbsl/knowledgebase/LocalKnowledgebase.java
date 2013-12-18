package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import org.aksw.autosparql.commons.index.Indices;
import com.hp.hpl.jena.rdf.model.Model;

public class LocalKnowledgebase extends Knowledgebase{
	
	private Model model;
	
	public LocalKnowledgebase(Model model, String label, String description,Indices indices)
	{
		super(label, description, indices);
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}

}