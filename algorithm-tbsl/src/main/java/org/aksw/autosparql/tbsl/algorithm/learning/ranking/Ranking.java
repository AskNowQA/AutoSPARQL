package org.aksw.autosparql.tbsl.algorithm.learning.ranking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.dllearner.utilities.MapUtils;

public class Ranking {

	public final Map<TemplateInstantiation, Double> templateInstantiation2Score = new HashMap<TemplateInstantiation, Double>();
	
	public Ranking() {
	}
	
	public Ranking(Map<TemplateInstantiation, Double> templateInstantiation2Score) {
		this.templateInstantiation2Score.putAll(templateInstantiation2Score);
	}
	
	public void add(Map<TemplateInstantiation, Double> templateInstantiation2Score){
		this.templateInstantiation2Score.putAll(templateInstantiation2Score);
	}
	
	public void add(Ranking ranking){
		this.templateInstantiation2Score.putAll(ranking.getTemplateInstantiationWithScore());
	}
	
	public void add(TemplateInstantiation templateInstantiation, double score){
		templateInstantiation2Score.put(templateInstantiation, score);
	}
	
	public Map<TemplateInstantiation, Double> getTemplateInstantiationWithScore() {
		return templateInstantiation2Score;
	}
	
	public List<TemplateInstantiation> getRankedTemplateInstantiations() {
		List<Entry<TemplateInstantiation, Double>> sortedByValues = MapUtils.sortByValues(templateInstantiation2Score);
		List<TemplateInstantiation> sortedTemplateInstantiations = new ArrayList<TemplateInstantiation>(sortedByValues.size());
		for (Entry<TemplateInstantiation,Double> entry : sortedByValues) {
			sortedTemplateInstantiations.add(entry.getKey());
		}
		return sortedTemplateInstantiations;
	}
	
	public TemplateInstantiation getBest(){
		return !templateInstantiation2Score.isEmpty() ? getRankedTemplateInstantiations().get(0) : null;
	}
	
	public List<TemplateInstantiation> getTopN(int n){
		List<TemplateInstantiation> rankedTemplateInstantiations = getRankedTemplateInstantiations();
		return rankedTemplateInstantiations.subList(0, Math.min(rankedTemplateInstantiations.size(), n));
	}
	
	public double getScore(TemplateInstantiation templateInstantiation){
		return templateInstantiation2Score.get(templateInstantiation);
	}

}
