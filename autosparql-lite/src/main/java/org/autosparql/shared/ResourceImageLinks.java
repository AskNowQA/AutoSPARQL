package org.autosparql.shared;

import java.util.HashMap;
import java.util.Map;

public final class ResourceImageLinks
{
	private ResourceImageLinks() {throw new AssertionError();}

	private static final String[][] allowedPrefixToImageArray = new String[][]{
		{"http://sws.geonames.org/","<img src=\"img/geonames_logo.gif\" alt=\"Geonames\" title=\"Geonames\"/>"},
		{"http://rdf.freebase.com/","<img src=\"img/Freebase-logo.png\" alt=\"Freebase\" title=\"Freebase\"/ width=\"50%\">"},
		{"http://en.wikipedia.org/","<img src=\"img/wikipedia-logo.svg\" alt=\"Wikipedia\" title=\"Wikipedia\" height=\"30\"/>"}
	};
	
	private static Map<String,String> prefixToImageMap = new HashMap<String,String>();
	static {
		for(String[] entry: allowedPrefixToImageArray) {prefixToImageMap.put(entry[0],entry[1]);}
	}

	public static String prefix(String uri)
	{
		for(String s : prefixToImageMap.keySet()){if(uri.startsWith(s)){return s;}}
		return null;
	}

	public static String getImage(String url)
	{
		String prefix;
		if((prefix=prefix(url))==null) {return null;}
		return prefixToImageMap.get(prefix);		
	}
		
	public static String getImageLink(String url) {return "<a href=\""+url+"\">"+getImage(url)+"</a>";}
}
