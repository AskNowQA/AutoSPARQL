package org.aksw.autosparql.algorithm.tbsl.learning.feature;

import org.aksw.autosparql.algorithm.tbsl.learning.TemplateInstantiation;

public interface FeatureExtractor {
	double extractFeature(TemplateInstantiation templateInstantiation);
	Feature getFeature();
}
