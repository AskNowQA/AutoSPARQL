package org.aksw.autosparql;

import com.google.common.collect.Lists;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

import java.util.List;

public enum Dataset {
	DBPEDIA("DBpedia", "http://dbpedia.org",
			Lists.newArrayList("http://dbpedia.org/resource/Dresden",
							   "http://dbpedia.org/resource/Leipzig"),
			Lists.newArrayList("dbo,http://dbpedia.org/ontology/",
							   "dbp,http://dbpedia.org/property/",
							   "dbr,http://dbpedia.org/resource/")),
	LINKEDMDB("LinkedMDB", "http://linkedmdb.org",
			  Lists.newArrayList("http://data.linkedmdb.org/resource/film/10283",
								 "http://data.linkedmdb.org/resource/film/10459"),
			  Lists.newArrayList("film,http://data.linkedmdb.org/resource/film/",
								 "movie,http://data.linkedmdb.org/resource/movie/")),
	BIOMEDICAL("Biomedical", "http://biomedical.org",
			   Lists.newArrayList("http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/1003",
								  "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/1004"),
			   Lists.newArrayList("diseasome,http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/",
								  "drugbank,http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/",
								  "sider,http://www4.wiwiss.fu-berlin.de/sider/resource/sider/drugs"));

	String label;
	String uri;
	List<String> examples;
	PrefixMapping prefixes;

	Dataset(String label, String uri, List<String> examples, List<String> prefixesList) {
		this.label = label;
		this.uri = uri;
		this.examples = examples;

		prefixes = new PrefixMappingImpl();
		prefixes.withDefaultMappings(PrefixMapping.Standard);

		prefixesList.forEach(e -> {
			String[] split = e.split(",");
			String prefix = split[0];
			String ns = split[1];
			prefixes.setNsPrefix(prefix, ns);
		});
	}

	public String getLabel() {
		return label;
	}
}