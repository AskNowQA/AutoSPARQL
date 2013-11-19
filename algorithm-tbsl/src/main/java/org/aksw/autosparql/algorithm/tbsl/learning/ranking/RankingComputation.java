package org.aksw.autosparql.algorithm.tbsl.learning.ranking;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.aksw.autosparql.algorithm.tbsl.learning.Entity;
import org.aksw.autosparql.algorithm.tbsl.learning.TemplateInstantiation;
import org.aksw.autosparql.algorithm.tbsl.sparql.Slot;
import org.aksw.autosparql.algorithm.tbsl.sparql.Template;

public interface RankingComputation {
	Ranking computeRanking(Template template, Collection<TemplateInstantiation> templateInstantiations, Map<Slot, Collection<Entity>> slot2Entites);
	Ranking computeRanking(Map<Template, List<TemplateInstantiation>> template2Instantiations, Map<Template, Map<Slot, Collection<Entity>>> template2Allocations, List<Double> parameters);
}
