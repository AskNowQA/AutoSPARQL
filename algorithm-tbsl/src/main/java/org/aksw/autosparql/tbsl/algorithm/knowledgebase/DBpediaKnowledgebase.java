package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.aksw.autosparql.tbsl.algorithm.util.SolrServerAksw;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class DBpediaKnowledgebase extends RemoteKnowledgebase
{
	public static final DBpediaKnowledgebase INSTANCE = new DBpediaKnowledgebase();

	static private SparqlEndpoint dbpediaEndpoint;

	static SparqlEndpoint getDbpediaEndpoint()
	{
		if(dbpediaEndpoint==null)
		{
			try{dbpediaEndpoint = new SparqlEndpoint(new URL("http://dbpedia.org/sparql"),Collections.<String>singletonList(""), Collections.<String>emptyList());}
			catch (MalformedURLException e){throw new RuntimeException(e);}
		}		
		return dbpediaEndpoint;
	}

	private static MappingBasedIndex createMappingIndex()
	{
		return new MappingBasedIndex(
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_class_mappings.txt").getPath(), 
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_resource_mappings.txt").getPath(),
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_dataproperty_mappings.txt").getPath(),
				DBpediaKnowledgebase.class.getClassLoader().getResource("tbsl/dbpedia_objectproperty_mappings.txt").getPath()
				);
	}	

	private DBpediaKnowledgebase() {super(getDbpediaEndpoint(),"dbpedia","DBpedia",SolrServerAksw.INSTANCE.dbpediaIndices);}
}