package org.autosparql.server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.autosparql.shared.Example;

import com.hp.hpl.jena.vocabulary.XSD;

public class Visualization
{
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
				if(object.contains("^^"))
				{
					Double.parseDouble(object.substring(object.lastIndexOf('^'+1)));
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
}