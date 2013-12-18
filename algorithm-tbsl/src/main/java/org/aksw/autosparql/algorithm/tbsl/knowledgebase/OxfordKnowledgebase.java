package org.aksw.autosparql.algorithm.tbsl.knowledgebase;

import org.aksw.autosparql.commons.index.Indices;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OxfordKnowledgebase extends LocalKnowledgebase
{
	// ugly, but best as I could do with enum pattern not possible because of subclass and constructor call having to be first :-( 
	static Model model;
	static
	{
		model = ModelFactory.createMemModelMaker().createDefaultModel();
		model.read(OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("oxford.ttl"),null,"TTL");
	}
	public static final OxfordKnowledgebase INSTANCE = new OxfordKnowledgebase();
	
	private OxfordKnowledgebase() {	super(model,"oxford","Oxford Houses",new Indices(model));}
}