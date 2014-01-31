package org.aksw.autosparql.tbsl.algorithm.learning.feature;

import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;

public abstract class AbstractFeatureExtractor implements FeatureExtractor{
	
	protected Knowledgebase knowledgebase;

	public AbstractFeatureExtractor(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}

}
