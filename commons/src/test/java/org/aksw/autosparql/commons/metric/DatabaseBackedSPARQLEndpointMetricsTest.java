package org.aksw.autosparql.commons.metric;

import static org.junit.Assert.assertTrue;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.junit.Test;

public class DatabaseBackedSPARQLEndpointMetricsTest {

	@Test
	public void testPmiMetric() throws Exception
	{

		String NS = "http://dbpedia.org/ontology/";
		String NS_Res = "http://dbpedia.org/resource/";

		NamedClass person = new NamedClass(NS + "Person");
		NamedClass writer = new NamedClass(NS + "Writer");
		NamedClass book = new NamedClass(NS + "Book");
		NamedClass film = new NamedClass(NS + "Film");
		NamedClass actor = new NamedClass(NS + "Actor");
		ObjectProperty pAuthor = new ObjectProperty(NS + "author");
		ObjectProperty pWriter = new ObjectProperty(NS + "writer");
		ObjectProperty pStarring = new ObjectProperty(NS + "starring");
		Individual bradPitt = new Individual(NS_Res + "Brad_Pitt");
		Individual bradPittBoxer = new Individual(NS_Res + "Brad_Pitt_%28boxer%29");
		Individual danBrown = new Individual(NS_Res + "Dan_Brown");
		Individual danBrowne = new Individual(NS_Res + "Dan_Browne");

		SPARQLEndpointMetrics pmiGen = SPARQLEndpointMetrics.getDbpediaMetrics();

		pmiGen.getPMI(new NamedClass(NS + "River"), new NamedClass(NS + "Film"));
		pmiGen.getDirectedPMI(pAuthor, person);

		System.out.println("#########################################");

		pmiGen.getDirectedPMI(pAuthor, writer);

		System.out.println("#########################################");

		pmiGen.getDirectedPMI(book, pAuthor);

		System.out.println("#########################################");

		pmiGen.getDirection(writer, pAuthor, book);

		System.out.println("#########################################");

//		pmiGen.getDirection(person, pStarring, film);
//
//		System.out.println("#########################################");
//
//		pmiGen.getMostFrequentProperties(person, film);
//
//		System.out.println("#########################################");
//
//		pmiGen.getMostFrequentProperties(film, actor);
//
//		System.out.println("#########################################");
//
//		pmiGen.getMostFrequentProperties(film, person);
//
//		System.out.println("#########################################");

		pmiGen.getOccurences(book);
		pmiGen.getOccurencesInObjectPosition(book);
		pmiGen.getOccurencesInSubjectPosition(book);

		System.out.println("#########################################");

//		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, person));
		{
		double g1,g2,g3,g4;
		System.out.println("Goodness: " + (g1=pmiGen.getGoodness(person, pAuthor, book)));
		System.out.println("Goodness: " + (g2=pmiGen.getGoodness(person, pWriter, book)));
		System.out.println("Goodness: " + (g3=pmiGen.getGoodness(book, pAuthor, person)));
		System.out.println("Goodness: " + (g4=pmiGen.getGoodness(book, pWriter, person)));
		assertTrue("getGoodness(book, pAuthor, person) not the highest",g3>Math.max(g4,Math.max(g1,g2)));
		}
//		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPitt));
//		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPittBoxer));
		{
		double g1,g2;
		System.out.println("Goodness: " + (g1=pmiGen.getGoodness(book, pAuthor, danBrown)));
		System.out.println("Goodness: " + (g2=pmiGen.getGoodness(book, pAuthor, danBrowne)));
		assertTrue("danBrowne higher score than danBrown",g1>g2);
		}
	}

}
