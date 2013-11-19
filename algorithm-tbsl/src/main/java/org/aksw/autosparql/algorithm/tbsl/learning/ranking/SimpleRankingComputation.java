package org.aksw.autosparql.algorithm.tbsl.learning.ranking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.aksw.autosparql.algorithm.tbsl.learning.Entity;
import org.aksw.autosparql.algorithm.tbsl.learning.TemplateInstantiation;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.EntityProminenceFeatureExtractor;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.EntityStringSimilarityFeatureExtractor;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.Feature;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.FeatureExtractor;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.TripleProbabilityFeatureExtractor;
import org.aksw.autosparql.algorithm.tbsl.sparql.Slot;
import org.aksw.autosparql.algorithm.tbsl.sparql.SlotType;
import org.aksw.autosparql.algorithm.tbsl.sparql.Template;
import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;
import org.aksw.autosparql.algorithm.tbsl.util.Prominences;
import org.aksw.autosparql.algorithm.tbsl.util.RemoteKnowledgebase;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.aksw.autosparql.commons.metric.SPARQLEndpointMetrics;

public class SimpleRankingComputation extends AbstractRankingComputation{
	
	
	private static final Logger logger = Logger.getLogger(SimpleRankingComputation.class.getName());
	
	private SPARQLEndpointMetrics metrics;
	private List<Feature> features = Arrays.asList(
			Feature.PROMINENCE_AVERAGE
			,Feature.STRING_SIMILARITY_AVERAGE
//			,Feature.TRIPLE_PROBABILITY
			);
	
	public SimpleRankingComputation(Knowledgebase knowledgebase) {
		super(knowledgebase);
		
		metrics = new SPARQLEndpointMetrics(((RemoteKnowledgebase) knowledgebase).getEndpoint(), new ExtractionDBCache("/opt/tbsl/dbpedia_pmi_cache_v2"));
	}

	@Override
	public Ranking computeRanking(Template template, Collection<TemplateInstantiation> templateInstantiations, Map<Slot, Collection<Entity>> slot2Entites) {
		return null;
	}
	
	@Override
	public Ranking computeRanking(Map<Template, List<TemplateInstantiation>> template2Instantiations,
			Map<Template, Map<Slot, Collection<Entity>>> template2Allocations, List<Double> parameters) {
		Ranking ranking = new Ranking();
		for (Entry<Template, List<TemplateInstantiation>> entry : template2Instantiations.entrySet()) {
			Template template = entry.getKey();
			
			//precompute the prominence scores here to be able to perform a min-max normalization 
			Map<Slot, Collection<Entity>> allocations = template2Allocations.get(template);
			Map<Slot, Prominences> entityProminenceScores = computeEntityProminenceScores(allocations);
			
			//create the feature extractors only once for each template
			Collection<FeatureExtractor> featureExtractors = createFeatureExtractors(entityProminenceScores);
			
			//extract for each template instantiation the selected features
			List<TemplateInstantiation> instantiations = entry.getValue();
			for (TemplateInstantiation templateInstantiation : instantiations) {
				try {
					for (FeatureExtractor featureExtractor : featureExtractors) {
						double value = featureExtractor.extractFeature(templateInstantiation);
						templateInstantiation.addFeature(featureExtractor.getFeature(), value);
						
					}
					//compute the overall score
					double score = 0;
					int index = 0;
					for(Entry<Feature, Double> feature2score : templateInstantiation.getFeaturesWithScore().entrySet()){
						score += feature2score.getValue() * parameters.get(index++);
					}
					ranking.add(templateInstantiation, score);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//print top n 
		for(TemplateInstantiation t : ranking.getTopN(5)){
			logger.debug(t.asQuery() + "(Score: " + ranking.getScore(t) + ")");
		}
		return ranking;
	}
	
	private Collection<FeatureExtractor> createFeatureExtractors(Map<Slot, Prominences> entityProminenceScores){
		Collection<FeatureExtractor> featureExtractors = new ArrayList<FeatureExtractor>();
		FeatureExtractor featureExtractor;
		for(Feature feature : features){
			if(feature == Feature.PROMINENCE_AVERAGE){
				featureExtractor = new EntityProminenceFeatureExtractor(knowledgebase, entityProminenceScores);
				featureExtractors.add(featureExtractor);
			} else if(feature == Feature.STRING_SIMILARITY_AVERAGE){
				featureExtractor = new EntityStringSimilarityFeatureExtractor(knowledgebase);
				featureExtractors.add(featureExtractor);
			} else if(feature == Feature.TRIPLE_PROBABILITY){
				featureExtractor = new TripleProbabilityFeatureExtractor(knowledgebase, metrics);
				featureExtractors.add(featureExtractor);
			}
		}
		return featureExtractors;
	}
	
	/**
	 * Compute the (unnormalized) prominence score for each entity depending on the slot type.
	 * @param slot2Allocations
	 */
	private Map<Slot, Prominences> computeEntityProminenceScores(Map<Slot, Collection<Entity>> slot2Allocations){
		Map<Slot, Prominences> prominenceScores = new HashMap<Slot, Prominences>();
		for (Entry<Slot, Collection<Entity>> entry : slot2Allocations.entrySet()) {
			Slot slot = entry.getKey();
			SlotType slotType = slot.getSlotType();
			Collection<Entity> entities = entry.getValue();
			Prominences entity2Prominence = new Prominences();
			for (Entity entity : entities) {
				double prominence = 0;
				if (slotType == SlotType.CLASS) {
					prominence = metrics.getOccurences(new NamedClass(entity.getURI()));
				} else if (slotType == SlotType.DATATYPEPROPERTY || slotType == SlotType.OBJECTPROPERTY
						|| slotType == SlotType.PROPERTY || slotType == SlotType.SYMPROPERTY) {
					prominence = metrics.getOccurences(new ObjectProperty(entity.getURI()));
				} else {
					prominence = metrics.getOccurencesInSubjectPosition(new Individual(entity.getURI()));
				}
				entity2Prominence.put(entity, prominence);
			}
			prominenceScores.put(slot, entity2Prominence);
		}
		return prominenceScores;
	}

}
