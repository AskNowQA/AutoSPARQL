package org.aksw.autosparql.shared;

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
"http://dbpedia.org/ontology/abstract",// too long for autosparql
"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",// TODO maybe visualized later in another view
"http://www.w3.org/2002/07/owl#sameAs"
			}));
}
