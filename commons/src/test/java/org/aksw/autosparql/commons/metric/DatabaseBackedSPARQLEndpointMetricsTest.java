package org.aksw.autosparql.commons.metric;

import static org.junit.Assert.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;

public class DatabaseBackedSPARQLEndpointMetricsTest {

	@Test
	public void testPmiMetric() throws Exception
	{
		//create database connection
		Class.forName("com.mysql.jdbc.Driver");
//		String dbHost = "linkedspending.aksw.org/sql";
		String dbHost = "[2001:638:902:2010:0:168:35:119]";		
		String dbPort = "3306";

		String database = "dbpedia_metrics";
		String dbUser = "tbsl";
		String dbPassword = "tbsl";
		System.out.println("jdbc:mysql://" + dbHost + ":"+ dbPort + "/" + database + "?" + "user=" + dbUser + "&"
		          + "password=" + dbPassword);
		Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":"+ dbPort + "/" + database + "?" + "user=" + dbUser + "&"
		          + "password=" + dbPassword);
		assertTrue(conn.isReadOnly());
		
		Logger.getLogger(DatabaseBackedSPARQLEndpointMetrics.class).setLevel(Level.DEBUG);
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lod.openlinksw.com/sparql"), "http://dbpedia.org");
		endpoint = SparqlEndpoint.getEndpointDBpedia();
		ExtractionDBCache cache = new ExtractionDBCache("/opt/tbsl/dbpedia_pmi_cache_v2");
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
		
		DatabaseBackedSPARQLEndpointMetrics pmiGen = new DatabaseBackedSPARQLEndpointMetrics(endpoint, cache, conn);
//		pmiGen.precompute(Arrays.asList(new String[]{"http://dbpedia.org/ontology/"}));
		
		System.out.println(pmiGen.getPMI(new NamedClass(NS + "River"), new NamedClass(NS + "Film")));
		
		System.out.println(pmiGen.getDirectedPMI(pAuthor, person));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(pAuthor, writer));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirectedPMI(book, pAuthor));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(writer, pAuthor, book));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getDirection(person, pStarring, film));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(person, film));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(film, actor));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getMostFrequentProperties(film, person));
		
		System.out.println("#########################################");
		
		System.out.println(pmiGen.getOccurences(book));
		System.out.println(pmiGen.getOccurencesInObjectPosition(book));
		System.out.println(pmiGen.getOccurencesInSubjectPosition(book));
		
		System.out.println("#########################################");
		
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, person));
		System.out.println("Goodness: " + pmiGen.getGoodness(person, pAuthor, book));
		System.out.println("Goodness: " + pmiGen.getGoodness(person, pWriter, book));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, person));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pWriter, person));
		
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPitt));
		System.out.println("Goodness: " + pmiGen.getGoodness(film, pStarring, bradPittBoxer));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, danBrown));
		System.out.println("Goodness: " + pmiGen.getGoodness(book, pAuthor, danBrowne));	
	}

}
