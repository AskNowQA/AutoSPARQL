package org.aksw.autosparql.commons.knowledgebase;

import org.aksw.rdfindex.Indices;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

public class LocalKnowledgebase extends Knowledgebase{
	
	protected Model model;
	
	public LocalKnowledgebase(Model model, String label, String description,Indices indices)
	{
		super(label, description, indices);
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}

	@Override public ResultSet querySelect(String query)
	{
		return QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();
	}

}