package org.aksw.autosparql.algorithm.tbsl.learning.feature;

import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;

public abstract class AbstractFeatureExtractor implements FeatureExtractor{
	
	protected Knowledgebase knowledgebase;

	public AbstractFeatureExtractor(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}

}
