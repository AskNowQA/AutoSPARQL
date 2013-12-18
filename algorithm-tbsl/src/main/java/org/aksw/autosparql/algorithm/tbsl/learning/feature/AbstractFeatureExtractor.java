package org.aksw.autosparql.algorithm.tbsl.learning.feature;

import org.aksw.autosparql.algorithm.tbsl.knowledgebase.Knowledgebase;

public abstract class AbstractFeatureExtractor implements FeatureExtractor{
	
	protected Knowledgebase knowledgebase;

	public AbstractFeatureExtractor(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}

}
