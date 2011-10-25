package org.autosparql.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlackList
{
	public static final Set<String> dbpedia=new HashSet<String>(Arrays.asList(new String[]{
"http://live.dbpedia.org/ontology/wikiPageExternalLink",
"http://xmlns.com/foaf/0.1/page",
"http://xmlns.com/foaf/0.1/homepage",
"http://live.dbpedia.org/property/wikiPageUsesTemplate",
"http://live.dbpedia.org/property/latDeg",
"http://live.dbpedia.org/property/latMin",
"http://live.dbpedia.org/property/latSec",
"http://live.dbpedia.org/property/lonDeg",
"http://live.dbpedia.org/property/lonMin",
"http://live.dbpedia.org/property/lonSec",
			}));
}
