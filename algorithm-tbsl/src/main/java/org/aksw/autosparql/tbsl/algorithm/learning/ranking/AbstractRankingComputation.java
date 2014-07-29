package org.aksw.autosparql.tbsl.algorithm.learning.ranking;

import org.aksw.autosparql.commons.knowledgebase.Knowledgebase;

public abstract class AbstractRankingComputation implements RankingComputation{

	protected Knowledgebase knowledgebase;
	
	public AbstractRankingComputation(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}
	
	public Knowledgebase getKnowledgebase() {
		return knowledgebase;
	}

}
