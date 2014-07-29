package org.aksw.autosparql.tbsl.algorithm.learning.ranking;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.autosparql.commons.knowledgebase.Knowledgebase;
import org.aksw.autosparql.commons.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.commons.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.commons.metric.SPARQLEndpointMetrics;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.learning.feature.EntityProminenceFeatureExtractor;
import org.aksw.autosparql.tbsl.algorithm.learning.feature.EntityStringSimilarityFeatureExtractor;
import org.aksw.autosparql.tbsl.algorithm.learning.feature.Feature;
import org.aksw.autosparql.tbsl.algorithm.learning.feature.FeatureExtractor;
import org.aksw.autosparql.tbsl.algorithm.learning.feature.TripleProbabilityFeatureExtractor;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.sparql.Template;
import org.aksw.autosparql.tbsl.algorithm.util.Prominences;
import org.aksw.autosparql.tbsl.algorithm.util.StringDisplay;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.reasoning.SPARQLReasoner;

public class SimpleRankingComputation extends AbstractRankingComputation{
	
	
	private static final Logger logger = Logger.getLogger(SimpleRankingComputation.class.getName());
	
	protected SPARQLReasoner reasoner;
//	private SPARQLEndpointMetrics metrics;
	private List<Feature> features = Arrays.asList(
			Feature.PROMINENCE_AVERAGE
			,Feature.STRING_SIMILARITY_AVERAGE
			,Feature.TRIPLE_PROBABILITY
			);
	
	public SimpleRankingComputation(Knowledgebase knowledgebase)
	{
		super(knowledgebase);
		if(knowledgebase instanceof RemoteKnowledgebase)
		{reasoner = new SPARQLReasoner(new SparqlEndpointKS(((RemoteKnowledgebase)knowledgebase).getEndpoint()));}
		else
		{reasoner = new SPARQLReasoner(new LocalModelBasedSparqlEndpointKS(((LocalKnowledgebase)knowledgebase).getModel()));}		
//		metrics = new SPARQLEndpointMetrics(((RemoteKnowledgebase) knowledgebase).getEndpoint(), new ExtractionDBCache("/opt/tbsl/dbpedia_pmi_cache_v2"));
	}

	@Override
	public Ranking computeRanking(Template template, Collection<TemplateInstantiation> templateInstantiations, Map<Slot, Collection<Entity>> slot2Entites) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Ranking computeRanking(Map<Template, List<TemplateInstantiation>> template2Instantiations,
			Map<Template, Map<Slot, Collection<Entity>>> template2Allocations, List<Double> weights) {		
		System.out.println();
		Ranking ranking = new Ranking();
		for (Entry<Template, List<TemplateInstantiation>> entry : template2Instantiations.entrySet()) {
			Template template = entry.getKey();
			
			//precompute the prominence scores here to be able to perform a min-max normalization 
			Map<Slot, Collection<Entity>> allocations = template2Allocations.get(template);
			Map<Slot, Prominences> entityProminenceScores = computeEntityProminenceScoresWithReasoner(allocations);
			
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
										
					for(Entry<Feature, Double> feature2score : templateInstantiation.getFeaturesWithScore().entrySet())
					{
						double weight = weights.size()>index?weights.get(index++):1;
						score += feature2score.getValue() * weight;
					}
					// penalize unallocated slots
					score*=(double)templateInstantiation.getAllocations().size()/template.getSlots().size();
					ranking.add(templateInstantiation, score);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// extra step: remove template instantiations from the top, whose slot types do not fit the types of the resources
		// it is done at this point, because it uses SPARQL and is thus slow, especially if using a remote endpoint
//		while(true)
//		{
//			break;
//		}
		//print top n 
		for(TemplateInstantiation t : ranking.getTopN(10)){
			logger.debug(StringDisplay.shortenSparqlQuery(t.asQuery().toString()) + " (Score: " + ranking.getScore(t) + ")");
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
			}
				else if(feature == Feature.TRIPLE_PROBABILITY){
				featureExtractor = new TripleProbabilityFeatureExtractor(knowledgebase, SPARQLEndpointMetrics.getDbpediaMetrics());
				featureExtractors.add(featureExtractor);
			}
		}
		return featureExtractors;
	}
	
//	/**
//	 * Compute the (unnormalized) prominence score for each entity depending on the slot type.
//	 * @param slot2Allocations
//	 */
//	private Map<Slot, Prominences> computeEntityProminenceScores(Map<Slot, Collection<Entity>> slot2Allocations){
//		Map<Slot, Prominences> prominenceScores = new HashMap<Slot, Prominences>();
//		for (Entry<Slot, Collection<Entity>> entry : slot2Allocations.entrySet()) {
//			Slot slot = entry.getKey();
//			SlotType slotType = slot.getSlotType();
//			Collection<Entity> entities = entry.getValue();
//			Prominences entity2Prominence = new Prominences();
//			for (Entity entity : entities) {
//				double prominence = 0;
//				if (slotType == SlotType.CLASS) {
//					prominence = metrics.getOccurences(new NamedClass(entity.getURI()));
//				} else if (slotType == SlotType.DATATYPEPROPERTY || slotType == SlotType.OBJECTPROPERTY
//						|| slotType == SlotType.PROPERTY || slotType == SlotType.SYMPROPERTY) {
//					prominence = metrics.getOccurences(new ObjectProperty(entity.getURI()));
//				} else {
//					prominence = metrics.getOccurencesInSubjectPosition(new Individual(entity.getURI()));
//				}
//				entity2Prominence.put(entity, prominence);
//			}
//			prominenceScores.put(slot, entity2Prominence);
//		}
//		return prominenceScores;
//	}

	/**
	 * Compute the (unnormalized) prominence score for each entity depending on the slot type.
	 * @param slot2Allocations
	 */
	 public Map<Slot, Prominences> computeEntityProminenceScoresWithReasoner(Map<Slot, Collection<Entity>> slot2Allocations)
	{
		Map<Slot, Prominences> prominenceScores = new HashMap<Slot, Prominences>();
		for (Entry<Slot, Collection<Entity>> entry : slot2Allocations.entrySet()) {
			Slot slot = entry.getKey();
			SlotType slotType = slot.getSlotType();
			Collection<Entity> entities = entry.getValue();
			Prominences entity2Prominence = new Prominences();
			for (Entity entity : entities) {
				double prominence = 0;
				if (slotType == SlotType.CLASS) {
					prominence = reasoner.getPopularity(new NamedClass(entity.getURI()));
				}
				else if(slotType == SlotType.DATATYPEPROPERTY)
				{
					prominence = reasoner.getPopularity(new DatatypeProperty(entity.getURI()));
				}				
				else if (slotType == SlotType.OBJECTPROPERTY
						|| slotType == SlotType.PROPERTY || slotType == SlotType.SYMPROPERTY) {
					prominence = reasoner.getPopularity(new ObjectProperty(entity.getURI()));
				} else {
					prominence = reasoner.getPopularity(new Individual(entity.getURI()));
//					prominence = reasoner.get(new Individual(entity.getURI()));
				}
				entity2Prominence.put(entity, prominence);
			}
			prominenceScores.put(slot, entity2Prominence);
		}
		return prominenceScores;
	}

}
