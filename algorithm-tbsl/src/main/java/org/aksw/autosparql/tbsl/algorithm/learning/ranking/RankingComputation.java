package org.aksw.autosparql.tbsl.algorithm.learning.ranking;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.Template;

public interface RankingComputation {
	Ranking computeRanking(Template template, Collection<TemplateInstantiation> templateInstantiations, Map<Slot, Collection<Entity>> slot2Entites);
	Ranking computeRanking(Map<Template, List<TemplateInstantiation>> template2Instantiations, Map<Template, Map<Slot, Collection<Entity>>> template2Allocations, List<Double> parameters);
}
