package org.aksw.autosparql.tbsl.algorithm.knowledgebase;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.aksw.autosparql.tbsl.algorithm.util.SolrServerAksw;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class DBpediaKnowledgebase extends RemoteKnowledgebase
{
	public static final DBpediaKnowledgebase INSTANCE = new DBpediaKnowledgebase();

	static private SparqlEndpoint createEndpoint()
	{
		try{return new SparqlEndpoint(new URL("http://dbpedia.org/sparql"),Collections.<String>singletonList(""), Collections.<String>emptyList());}
		catch (MalformedURLException e){throw new RuntimeException(e);}
	}

	private DBpediaKnowledgebase() {super(createEndpoint(),"dbpedia","DBpedia",SolrServerAksw.INSTANCE.dbpediaIndices);}
}