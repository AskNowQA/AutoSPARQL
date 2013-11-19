package org.aksw.autosparql.client;

public class Transformer
{
	/** split normal urls as well as abbreviated ones, also take the last part of things like "rdf-schema#comment" */
	public static String lastURLPart(String url)
	{
		String[] tokens=url.split("[:/#]");
		return tokens[tokens.length-1];
	}
	
	public static String displayProperty(String property)
	{
		return lastURLPart(property);
	}
	
	public static String displayObject(String object)
	{
		return lastURLPart(object);		
	}
}