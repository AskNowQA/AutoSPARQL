package org.aksw.autosparql.tbsl.algorithm.learning.feature;

import java.util.Set;

import org.aksw.autosparql.commons.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.util.TriplePatternExtractor;

import com.hp.hpl.jena.graph.Triple;

public class NumberOfTriplePatternFeatureExtractor extends AbstractFeatureExtractor{

	private TriplePatternExtractor extractor = new TriplePatternExtractor();

	public NumberOfTriplePatternFeatureExtractor(Knowledgebase knowledgebase) {
		super(knowledgebase);
	}

	@Override
	public double extractFeature(TemplateInstantiation templateInstantiation) {
		Set<Triple> triplePattern = extractor.extractTriplePattern(templateInstantiation.asQuery());
		return triplePattern.size();
	}

	@Override
	public Feature getFeature() {
		return Feature.NUMBER_OF_TRIPLE_PATTERN;
	}


}
