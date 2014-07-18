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
		//IPV6
				String protocol = "tcp";
				String connStr = "jdbc:mysql://address="
						+ "(protocol=" + protocol + ")"
								+ "(host=" + dbHost + ")"
										+ "(port=" + dbPort + ")"
												+ "/" + database;
		Connection conn = DriverManager.getConnection(connStr, dbUser, dbPassword);
		conn.setReadOnly(true);
		
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(DatabaseBackedSPARQLEndpointMetrics.class).setLevel(Level.DEBUG);
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://linkedspending.aksw.org/sparql"), "http://dbpedia.org");
		ExtractionDBCache cache = new ExtractionDBCache("cache");
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
		
		pmiGen.getPMI(new NamedClass(NS + "River"), new NamedClass(NS + "Film"));	
		pmiGen.getDirectedPMI(pAuthor, person);
		
		System.out.println("#########################################");
		
		pmiGen.getDirectedPMI(pAuthor, writer);
		
		System.out.println("#########################################");
		
		pmiGen.getDirectedPMI(book, pAuthor);
		
		System.out.println("#########################################");
		
		pmiGen.getDirection(writer, pAuthor, book);
		
		System.out.println("#########################################");
		
		pmiGen.getDirection(person, pStarring, film);
		
		System.out.println("#########################################");
		
		pmiGen.getMostFrequentProperties(person, film);
		
		System.out.println("#########################################");
		
		pmiGen.getMostFrequentProperties(film, actor);
		
		System.out.println("#########################################");
		
		pmiGen.getMostFrequentProperties(film, person);
		
		System.out.println("#########################################");
		
		pmiGen.getOccurences(book);
		pmiGen.getOccurencesInObjectPosition(book);
		pmiGen.getOccurencesInSubjectPosition(book);
		
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
