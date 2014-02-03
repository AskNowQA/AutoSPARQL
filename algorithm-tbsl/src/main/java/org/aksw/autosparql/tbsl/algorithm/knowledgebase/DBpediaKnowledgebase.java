package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class DBpediaKnowledgebase extends RemoteKnowledgebase
{
	public static final DBpediaKnowledgebase INSTANCE = new DBpediaKnowledgebase();

	static private SparqlEndpoint createEndpoint()
	{
		try{return new SparqlEndpoint(new URL("http://dbpedia.org/sparql"),Collections.<String>singletonList(""), Collections.<String>emptyList());}
		catch (MalformedURLException e){throw new RuntimeException(e);}
	}
	static SparqlEndpoint endpoint = createEndpoint();
	
	private static MappingBasedIndex createMappingIndex()
	{
		return new MappingBasedIndex(
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_class_mappings.txt").getPath(), 
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_resource_mappings.txt").getPath(),
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_dataproperty_mappings.txt").getPath(),
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_objectproperty_mappings.txt").getPath()
				);
	}	

	private DBpediaKnowledgebase() {super(endpoint,"dbpedia","DBpedia",new Indices(endpoint,createMappingIndex()));}
}