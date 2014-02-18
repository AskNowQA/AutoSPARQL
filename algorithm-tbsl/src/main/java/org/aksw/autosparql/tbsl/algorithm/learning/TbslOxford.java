package org.aksw.autosparql.tbsl.algorithm.learning;

import org.aksw.autosparql.tbsl.algorithm.knowledgebase.OxfordKnowledgebase;

public class TbslOxford extends TBSL 
{
	public static final TBSL INSTANCE = new TbslOxford();
	
	private TbslOxford()
	{
		super(OxfordKnowledgebase.INSTANCE,new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
//		PopularityMap map = new PopularityMap(this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(),
//				 new SparqlQueriable(((RemoteKnowledgebase)this.knowledgebase).getEndpoint(), "cache"));
//
//		this.setUseDomainRangeRestriction(false);
//		this.setPopularityMap(null);
	}
}