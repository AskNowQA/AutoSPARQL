package org.aksw.autosparql.commons.knowledgebase;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.aksw.autosparql.commons.index.SolrServer;
import org.aksw.rdfindex.MappingBasedIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class DBpediaKnowledgebase extends RemoteKnowledgebase
{
	public static final DBpediaKnowledgebase INSTANCE = new DBpediaKnowledgebase();

	static private SparqlEndpoint dbpediaEndpoint;

	static public SparqlEndpoint getDbpediaEndpoint()
	{
		if(dbpediaEndpoint==null)
		{
			try{dbpediaEndpoint = new SparqlEndpoint(new URL("http://linkedspending.aksw.org/sparql"),Collections.<String>singletonList("http://dbpedia.org"), Collections.<String>emptyList());}
			catch (MalformedURLException e){throw new RuntimeException(e);}
		}
		return dbpediaEndpoint;
	}

	private static MappingBasedIndex createMappingIndex()
	{
		try
		{
			return new MappingBasedIndex(
					DBpediaKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/dbpedia_class_mappings.txt"),
					DBpediaKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/dbpedia_resource_mappings.txt"),
					DBpediaKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/dbpedia_dataproperty_mappings.txt"),
					DBpediaKnowledgebase.class.getClassLoader().getResourceAsStream("tbsl/dbpedia_objectproperty_mappings.txt")
					);
		}
		catch (IOException e) {throw new RuntimeException(e);}
	}

	private DBpediaKnowledgebase() {super(getDbpediaEndpoint(),"dbpedia","DBpedia",SolrServer.INSTANCE.getIndices());}
}