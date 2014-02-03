package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.MappingBasedIndex;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OxfordKnowledgebase extends LocalKnowledgebase
{
	public static final OxfordKnowledgebase INSTANCE = new OxfordKnowledgebase();
 
	static Model model;
	static
	{
		model = ModelFactory.createMemModelMaker().createDefaultModel();
		model.read(OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("oxford.ntriples"),null,"N-TRIPLE");		
	}
	
	private static MappingBasedIndex createMappingIndex()
	{
		return new MappingBasedIndex(
				OxfordKnowledgebase.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
				OxfordKnowledgebase.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
				OxfordKnowledgebase.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
				OxfordKnowledgebase.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
				);
	}	

	private OxfordKnowledgebase() {	super(model,"oxford","Oxford Houses",new Indices(model,createMappingIndex()));}
}