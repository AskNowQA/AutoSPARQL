package org.aksw.autosparql.tbsl.algorithm;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.aksw.autosparql.commons.index.Indices;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.DBpediaKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.OxfordKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TBSL;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslDbpedia;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslOxford;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.learning.ranking.SimpleRankingComputation;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.util.Prominences;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TBSLTest extends TestCase
{
	@Test
	public void testDBpedia() throws Exception
	{
		String question = "Give me all books written by Dan Brown";
		TemplateInstantiation ti = TbslDbpedia.INSTANCE.answerQuestion(question);
		ResultSet rs = DBpediaKnowledgebase.INSTANCE.querySelect(ti.getQuery());
		//		assertTrue(rs.nextSolution().toString().contains("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
		System.out.println(ti.getQuery());
		System.out.println(rs.nextSolution());
	}

	@Test
	public void testProminence()
	{
		Entity bedrooms = new Entity("http://diadem.cs.ox.ac.uk/ontologies/real-estate#bedrooms", "number of bedrooms");		
		Slot slot = new Slot("p1",SlotType.DATATYPEPROPERTY,Arrays.asList(new String[]{"BEDROOMS"}));		
		HashMap<Slot, Collection<Entity>> map = new HashMap<>();
		map.put(slot, Collections.singleton(bedrooms));
		Map<Slot, Prominences> scores = new SimpleRankingComputation(OxfordKnowledgebase.INSTANCE).computeEntityProminenceScoresWithReasoner(map);
		assertTrue(scores.values().iterator().next().values().iterator().next()>800);
	}

	@Test
	public void testOxford() throws Exception
	{
		//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(model, resourcesIndex, classesIndex, propertiesIndex);
		String question = "Give me all houses with more than 3 bedrooms.";// and more than 2 bedrooms
		TemplateInstantiation ti = TbslOxford.INSTANCE.answerQuestion(question);
		//		System.out.println("Learned query:\n" + ti.getQuery());
		ResultSet rs = OxfordKnowledgebase.INSTANCE.querySelect(ti.getQuery());
		assertTrue(rs.nextSolution().toString().contains("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
		//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		//		System.out.println(learner.getLearnedPosition());
	}

}