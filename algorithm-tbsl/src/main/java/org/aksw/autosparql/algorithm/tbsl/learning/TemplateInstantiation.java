package org.aksw.autosparql.algorithm.tbsl.learning;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.aksw.autosparql.algorithm.tbsl.learning.feature.Feature;
import org.aksw.autosparql.algorithm.tbsl.sparql.Slot;
import org.aksw.autosparql.algorithm.tbsl.sparql.Template;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;

public class TemplateInstantiation {
	
	private Template template;
	private Map<Slot, Entity> allocations;
	
	private Map<Feature, Double> feature2Score = new LinkedHashMap<Feature, Double>();
	
	public TemplateInstantiation(Template template, Map<Slot, Entity> allocations) {
		this.template = template;
		this.allocations = allocations;
	}
	
	public Map<Feature, Double> getFeaturesWithScore() {
		return feature2Score;
	}
	
	public Query asQuery(){
		ParameterizedSparqlString query = new ParameterizedSparqlString(template.getQuery().toString());
		
		for (Entry<Slot, Entity> entry : allocations.entrySet()) {
			Slot slot = entry.getKey();
			Entity entity = entry.getValue();
			
			query.setIri(slot.getAnchor(), entity.getURI());
		}
		return query.asQuery();
	}
	
	public String getQuery(){
		ParameterizedSparqlString query = new ParameterizedSparqlString(template.getQuery().toString());
		
		for (Entry<Slot, Entity> entry : allocations.entrySet()) {
			Slot slot = entry.getKey();
			Entity entity = entry.getValue();
			
			query.setIri(slot.getAnchor(), entity.getURI());
		}
		return query.toString();
	}
	
	public Template getTemplate() {
		return template;
	}
	
	public Map<Slot, Entity> getAllocations() {
		return allocations;
	}
	
	@Override
	public String toString() {
		return asQuery().toString();
	}
	
	public void addFeature(Feature feature, double score){
		feature2Score.put(feature, score);
	}

//	@Override
//	public int hashCode() {
//		return asQuery().hashCode();
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		TemplateInstantiation other = (TemplateInstantiation) obj;
//		return asQuery().equals(other.asQuery());
//	}
	
}
