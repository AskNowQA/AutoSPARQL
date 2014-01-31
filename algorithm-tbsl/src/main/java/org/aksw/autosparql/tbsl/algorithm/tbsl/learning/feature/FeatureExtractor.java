package org.aksw.autosparql.tbsl.algorithm.learning.feature;

import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;

public interface FeatureExtractor {
	double extractFeature(TemplateInstantiation templateInstantiation);
	Feature getFeature();
}
