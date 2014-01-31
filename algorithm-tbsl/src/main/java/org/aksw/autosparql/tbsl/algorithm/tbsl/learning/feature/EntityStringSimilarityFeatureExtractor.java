package org.aksw.autosparql.tbsl.algorithm.learning.feature;

import java.util.Map;
import java.util.Map.Entry;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.util.Similarity;

/**
 * Computes a score <b>S</b> by:<br/>
 * Sum up the maximum of the String similarity value <b>s</b> between the entity label <b>l</b> and the tokens t_1,...,t_n in each slot, 
 * i.e.<br/>
 * <b>S</b>=SUM(MAX(s(l, t_n)))/#slots
 * @author Lorenz Buehmann
 * 
 * 
 * 				
 */
public class EntityStringSimilarityFeatureExtractor extends AbstractFeatureExtractor{
	
	public EntityStringSimilarityFeatureExtractor(Knowledgebase knowledgebase) {
		super(knowledgebase);
	}

	@Override
	public double extractFeature(TemplateInstantiation templateInstantiation) {
		double total = 0;
		Map<Slot, Entity> allocations = templateInstantiation.getAllocations();
		for(Entry<Slot, Entity> entry : allocations.entrySet()){
			Slot slot = entry.getKey();
			Entity entity = entry.getValue();
			String label = entity.getLabel();
			double similarity = 0;
			for (String word : slot.getWords()) {
				similarity = Math.max(similarity, Similarity.getSimilarity(word, label));
			}
			total += similarity;
		}
		//compute the average
		total /= allocations.size();
		return total;
	}
	
	@Override
	public Feature getFeature() {
		return Feature.STRING_SIMILARITY_AVERAGE;
	}


}
