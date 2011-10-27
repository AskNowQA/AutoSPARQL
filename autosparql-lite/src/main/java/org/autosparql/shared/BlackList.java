package org.autosparql.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlackList
{
	public static final Set<String> dbpedia=new HashSet<String>(Arrays.asList(new String[]{
"http://dbpedia.org/ontology/wikiPageExternalLink",
//"http://xmlns.com/foaf/0.1/page",
//"http://xmlns.com/foaf/0.1/homepage",
"http://dbpedia.org/property/wikiPageUsesTemplate",
"http://dbpedia.org/property/latDeg",
"http://dbpedia.org/property/latMin",
"http://dbpedia.org/property/latSec",
"http://dbpedia.org/property/lonDeg",
"http://dbpedia.org/property/lonMin",
"http://dbpedia.org/property/lonSec",
			}));
}
