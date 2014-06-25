package org.aksw.autosparql.tbsl.algorithm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.DBpediaKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.OxfordKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslDbpedia;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslOxford;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.learning.ranking.SimpleRankingComputation;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.util.Prominences;
import org.junit.Test;
import com.hp.hpl.jena.query.ResultSet;

public class TBSLTest extends TestCase
{
	@Test
	public void testDBpedia() throws Exception
	{
//		String question = "Give me soccer clubs in Premier League.";
		String question = "Give me all books written by Dan Browns.";
		TemplateInstantiation ti = TbslDbpedia.INSTANCE.answerQuestion(question);
		ResultSet rs = DBpediaKnowledgebase.INSTANCE.querySelect(ti.getQuery());
		System.out.println(rs.nextSolution().toString());		
//		assertTrue(rs.nextSolution().toString().contains("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
//		System.out.println(ti.getQuery());
//		System.out.println(rs.nextSolution());
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
		assertFalse(((LocalKnowledgebase)TbslOxford.INSTANCE.getKnowledgebase()).getModel().isEmpty());
		String question = "Give me all houses with more than 3 bedrooms.";// and more than 2 bedrooms
		TemplateInstantiation ti = TbslOxford.INSTANCE.answerQuestion(question);
		ResultSet rs = OxfordKnowledgebase.INSTANCE.querySelect(ti.getQuery());
		assertTrue(rs.nextSolution().toString().contains("http://diadem.cs.ox.ac.uk/ontologies/real-estate#"));
	}

}