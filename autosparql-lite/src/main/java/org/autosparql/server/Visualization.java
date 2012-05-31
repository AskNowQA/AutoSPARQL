package org.autosparql.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.autosparql.shared.Example;

public class Visualization
{
	// in dbpedia, there are 52587 instances of type rdf:Property, 868 of them instances of DatatypeProperty, 858 of them with rdfs:range	

	// dbo:Temperature removed because there were very little properties // if it's kelvin we know the unit but what if not?
	static final Set<String> TEMPERATURE_PROPERTIES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] 
		{
		"http://dbpedia.org/datatype/kelvin",
		//"http://dbpedia.org/ontology/Temperature"
		})));

	// use these for line charts
	static final Set<String> NUMERIC_PROPERTIES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]
		{		
		"http://www.w3.org/2001/XMLSchema#float",
		"http://www.w3.org/2001/XMLSchema#double",
//		"http://www.w3.org/2001/XMLSchema#gYear",
//		"http://www.w3.org/2001/XMLSchema#gYearMonth",
		"http://www.w3.org/2001/XMLSchema#positiveInteger",
		"http://www.w3.org/2001/XMLSchema#integer",
//		"http://dbpedia.org/ontology/FlowRate",
		//"http://dbpedia.org/ontology/PopulationDensity",                                                
//		"http://dbpedia.org/ontology/Mass",
//		"http://dbpedia.org/ontology/Volume",
//		"http://dbpedia.org/ontology/Speed",
//		"http://dbpedia.org/ontology/Density",
//		"http://dbpedia.org/ontology/Power",
//		"http://dbpedia.org/ontology/Frequency",
//		"http://dbpedia.org/ontology/Voltage",
//		"http://dbpedia.org/ontology/Energy"
		})));

	// do a pie chart for this
	static final String BOOLEAN_PROPERTY = "http://www.w3.org/2001/XMLSchema#boolean";

	/**Generates a chart that visualizes the numeric properties of a set of instances described by the given SPARQL query.
	 * The chart is given as an html String describing a div tag that uses <a href="http://code.google.com/p/sgvizler/">sgvizler</a>.
	 * The returned String can be inserted into html code without further requirements. 	
	 * @param examples
	 * @param sparqlQuery
	 * @param endpoint
	 * @return
	 */
	public static String visualize(Set<Example> examples, String sparqlQuery, String endpoint)
	{
		//		for(Example example: examples)
		//		{
		//			
		//			"^^http://www.w3.org/2001/XMLSchema#integer";
		//		}
		if(examples.isEmpty()) {return "";}
		Example example = examples.iterator().next(); // TODO: maybe don't use only the first example?
		Map<String,Object> properties = example.getProperties();
		Set<String> numberProperties = new HashSet<String>();
		for(String property : properties.keySet())
		{
			String object = properties.get(property).toString(); 
			//System.out.println(object);
			//System.out.println(XSD.INTEGER.toStringID());
			//			System.out.println(XSD.integer.getURI());
			//			System.out.println(XSD.xint.getURI());
			//			if(object.contains(XSD.integer.getURI())||object.contains(XSD.xint.getURI()))
			try
			{
				Double.parseDouble(object);
				numberProperties.add(property);
			} catch(NumberFormatException e)
			{
				//int startOfNumber = object.lastIndexOf('^')+1;
				if(object.contains("^^"))
				{
					String value = object.substring(0,object.indexOf('^'));
					System.out.println(value);
					Double.parseDouble(value);
					numberProperties.add(property);
				}
			} 
		}
		String newQuery = sparqlQuery;

		String html = "<div id=\"sgvzl_example1\"\n"+
				"data-sgvizler-endpoint=\"\n"+endpoint+'"'+
				"data-sgvizler-query=\"\n"+newQuery+'"'+
				"data-sgvizler-chart=\"gPieChart\"\n"+
				"style=\"width:800px; height:400px;\"></div>";
		System.out.println(html);
		return html;
	}

//	public static String visualize(String sparqlQuery, String endpoint, String graph, List<String> properties)
//	{
//
//		Set<String> numberProperties = new HashSet<String>();
//		for(String property : properties.keySet())
//		{
//			String object = properties.get(property).toString(); 
//			//System.out.println(object);
//			//System.out.println(XSD.INTEGER.toStringID());
//			//			System.out.println(XSD.integer.getURI());
//			//			System.out.println(XSD.xint.getURI());
//			//			if(object.contains(XSD.integer.getURI())||object.contains(XSD.xint.getURI()))
//			try
//			{
//				Double.parseDouble(object);
//				numberProperties.add(property);
//			} catch(NumberFormatException e)
//			{
//				//int startOfNumber = object.lastIndexOf('^')+1;
//				if(object.contains("^^"))
//				{
//					String value = object.substring(0,object.indexOf('^'));
//					System.out.println(value);
//					Double.parseDouble(value);
//					numberProperties.add(property);
//				}
//			} 
//		}
//		String newQuery = sparqlQuery;
//
//		String html = "<div id=\"sgvzl_example1\"\n"+
//				"data-sgvizler-endpoint=\"\n"+endpoint+'"'+
//				"data-sgvizler-query=\"\n"+newQuery+'"'+
//				"data-sgvizler-chart=\"gPieChart\"\n"+
//				"style=\"width:800px; height:400px;\"></div>";
//		System.out.println(html);
//		return html;
//	}
}