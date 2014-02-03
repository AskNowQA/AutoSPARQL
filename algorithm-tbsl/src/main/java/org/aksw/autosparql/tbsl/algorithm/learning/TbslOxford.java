package org.aksw.autosparql.tbsl.algorithm.learning;

import org.aksw.autosparql.commons.sparql.SparqlQueriable;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.DBpediaKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.util.PopularityMap;

public class TbslOxford extends TBSL 
{
	public static final TBSL INSTANCE = new TbslOxford();
	
	private TbslOxford()
	{
		super(DBpediaKnowledgebase.INSTANCE,new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
//		PopularityMap map = new PopularityMap(this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(),
//				 new SparqlQueriable(((RemoteKnowledgebase)this.knowledgebase).getEndpoint(), "cache"));
//
//		this.setUseDomainRangeRestriction(false);
//		this.setPopularityMap(null);
	}
}