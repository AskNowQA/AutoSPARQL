package org.aksw.autosparql.algorithm.tbsl.learning.ranking;

import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;

public abstract class AbstractRankingComputation implements RankingComputation{

	protected Knowledgebase knowledgebase;
	
	public AbstractRankingComputation(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}
	
	public Knowledgebase getKnowledgebase() {
		return knowledgebase;
	}

}
