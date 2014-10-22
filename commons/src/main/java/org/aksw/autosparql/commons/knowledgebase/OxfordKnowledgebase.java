package org.aksw.autosparql.commons.knowledgebase;

import java.io.IOException;
import org.aksw.autosparql.commons.index.LemmatizedIndex;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.rdfindex.Indices;
import org.aksw.rdfindex.MappingBasedIndex;
import org.aksw.rdfindex.SPARQLClassesIndex;
import org.aksw.rdfindex.SPARQLDatatypePropertiesIndex;
import org.aksw.rdfindex.SPARQLIndex;
import org.aksw.rdfindex.SPARQLObjectPropertiesIndex;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class OxfordKnowledgebase extends LocalKnowledgebase
{
	static final protected Model oxfordModel;
//	 synchronized protected Model getOxfordModel()
	 static
	{
//		if(oxfordModel==null)
		{
			oxfordModel = ModelFactory.createMemModelMaker().createDefaultModel();
			oxfordModel.read(OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("oxford.ntriples"),null,"N-TRIPLE");
		}
//		return oxfordModel;
	}
	static final QueryExecutionFactory qef = new QueryExecutionFactoryModel(oxfordModel);

	public static final OxfordKnowledgebase INSTANCE = new OxfordKnowledgebase();


	private static MappingBasedIndex createMappingIndex()
	{
		try
		{
			return new MappingBasedIndex(
					OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/oxford_class_mappings.txt"),
					OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/oxford_resource_mappings.txt"),
					OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/oxford_dataproperty_mappings.txt"),
					OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/oxford_objectproperty_mappings.txt")
					);
		}
		catch (IOException e) {throw new RuntimeException(e);}
	}

//	private OxfordKnowledgebase() {	super(getOxfordModel(),"oxford","Oxford Houses",new Indices(getOxfordModel(),createMappingIndex()));}
	private OxfordKnowledgebase()
	{
		super(oxfordModel,"oxford","Oxford Houses",
			new Indices(new LemmatizedIndex(new SPARQLIndex(qef)), new LemmatizedIndex(new SPARQLClassesIndex(qef)), new LemmatizedIndex(new SPARQLObjectPropertiesIndex(qef)), new LemmatizedIndex(new SPARQLDatatypePropertiesIndex(qef)),createMappingIndex()));}
}