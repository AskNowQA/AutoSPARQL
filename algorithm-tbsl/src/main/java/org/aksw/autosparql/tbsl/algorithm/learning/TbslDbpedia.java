package org.aksw.autosparql.tbsl.algorithm.learning;

import org.aksw.autosparql.commons.knowledgebase.DBpediaKnowledgebase;

public class TbslDbpedia extends TBSL
{
	public static final TBSL INSTANCE = new TbslDbpedia();

	private TbslDbpedia()
	{
		super(DBpediaKnowledgebase.INSTANCE,new String[]{"tbsl/lexicon/english.lex"});
//		PopularityMap map = new PopularityMap(this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(),
//				 new SparqlQueriable(((RemoteKnowledgebase)this.knowledgebase).getEndpoint(), "cache"));
//
//		this.setUseDomainRangeRestriction(true);
//		this.setPopularityMap(map);
	}
}