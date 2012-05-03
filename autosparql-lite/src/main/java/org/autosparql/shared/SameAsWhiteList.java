package org.autosparql.shared;

import java.util.HashMap;
import java.util.Map;


public class SameAsWhiteList {
	
	public static final String[][] allowedPrefixToImageArray = new String[][]{
		{"http://sws.geonames.org/","<img src=\"img/geonames_logo.gif\" alt=\"Geonames\" title=\"Geonames\"/>"},
		{"http://rdf.freebase.com/","<img src=\"img/Freebase-logo.png\" alt=\"Freebase\" title=\"Freebase\"/ width=\"50%\">"},
		{"http://en.wikipedia.org/","<img src=\"img/wikipedia-logo.svg\" alt=\"Wikipedia\" title=\"Wikipedia\" height=\"30\"/>"}
	};
	private static Map<String,String> prefixToImageMap = new HashMap<String,String>();
	static {
		for(String[] entry: allowedPrefixToImageArray) {prefixToImageMap.put(entry[0],entry[1]);}

	}
	
	public static String isAllowed(String uri){
		for(String s : prefixToImageMap.keySet()){
			if(uri.startsWith(s)){
				return s;
			}
		}
		return null;
	}
	
	public static String getImageLink(String url)
	{//TODO keep in mind that this code works only if all keys have the same length
		return prefixToImageMap.get(url.substring(0, allowedPrefixToImageArray[0][0].length()));
	}
	
	public static void main(String[] args) {
		System.out.println(SameAsWhiteList.prefixToImageMap);
		System.out.println(SameAsWhiteList.getImageLink("http://en.wikipedia.org/"));
	}

}
