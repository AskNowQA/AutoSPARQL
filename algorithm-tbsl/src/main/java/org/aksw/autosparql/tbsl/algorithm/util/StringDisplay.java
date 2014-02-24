package org.aksw.autosparql.tbsl.algorithm.util;

public class StringDisplay
{
	public static String shortenSparqlQuery(String s)
	{
		return s
		.replaceAll("WHERE","")
		.replaceAll("PREFIX\\s*rdfs:\\s*<http://www.w3.org/2000/01/rdf-schema#>","")
		.replaceAll("PREFIX\\s*rdf:\\s*<http://www.w3.org/1999/02/22-rdf-syntax-ns#>","")
		.replaceAll("\n"," ")
		.replaceAll("\\s+"," ")
		.replaceAll("^\\s+","");
	}
}
