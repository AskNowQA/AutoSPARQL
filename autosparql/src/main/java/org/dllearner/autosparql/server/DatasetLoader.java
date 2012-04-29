package org.dllearner.autosparql.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.autosparql.server.search.Search;
import org.dllearner.autosparql.server.search.SolrSearch;
import org.dllearner.autosparql.server.search.VirtuosoSearch;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class DatasetLoader {
	
	public static List<Dataset> loadDatasets(String path){
		List<Dataset> datasets = new ArrayList<Dataset>();
		
		try {
			XMLConfiguration config = new XMLConfiguration(new File(path));
			
			List datasetConfigurations = config.configurationsAt("dataset");
			for(Iterator iter = datasetConfigurations.iterator();iter.hasNext();){
				HierarchicalConfiguration datasetConf = (HierarchicalConfiguration) iter.next();
				datasets.add(createDataset(datasetConf));
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		return datasets;
	}
	
	private static Dataset createDataset(HierarchicalConfiguration conf){
		HierarchicalConfiguration endpointConf = conf.configurationAt("endpoint");
		SPARQLEndpointEx endpoint = createEndpoint(endpointConf);
		
		HierarchicalConfiguration searchIndexConf = conf.configurationAt("index");
		Search searchIndex = createSearchIndex(searchIndexConf);
		
		return new Dataset(searchIndex, endpoint);
	}
	
	private static SPARQLEndpointEx createEndpoint(HierarchicalConfiguration conf){
		try {
			URL url = new URL(conf.getString("url"));
			String label = conf.getString("label");
			String prefix = conf.getString("prefix");
			if(prefix == null){
				prefix = label.replaceAll("@", "").replaceAll(" ", "");
			}
			String defaultGraphURI = conf.getString("defaultGraphURI");
			List<String> namedGraphURIs = conf.getList("namedGraphURI");
			List<String> predicateFilters = conf.getList("predicateFilters.predicate");
			
			return new SPARQLEndpointEx(url, Collections.singletonList(defaultGraphURI), namedGraphURIs, label, prefix, new HashSet<String>(predicateFilters));
		} catch (MalformedURLException e) {
			System.err.println("Could not parse URL from SPARQL endpoint.");
			e.printStackTrace();
		}
		return null;
	}
	
	private static Search createSearchIndex(HierarchicalConfiguration indexConf){
		Search searchIndex = null;
		if(indexConf.getProperty("solr.url") != null){
			HierarchicalConfiguration solrConf = indexConf.configurationAt("solr");
			String url = solrConf.getString("url");
			searchIndex = new SolrSearch(url);
		} else if(indexConf.getProperty("sparql.url") != null){
			HierarchicalConfiguration sparqlConf = indexConf.configurationAt("sparql");
			try {
				URL url = new URL(sparqlConf.getString("url"));
				String defaultGraphURI = sparqlConf.getString("defaultGraphURI");
				HierarchicalConfiguration namedGraphsConf = sparqlConf.configurationAt("namedGraphURIs");
				List<String> namedGraphURIs = new ArrayList<String>();
				for(Object o : namedGraphsConf.getList("namedGraphURI")){
					namedGraphURIs.add(o.toString());
				}
				Boolean isVirtuoso = (Boolean) sparqlConf.getProperty("virtuoso");
				if(isVirtuoso == null || !isVirtuoso){
					searchIndex = new org.dllearner.autosparql.server.search.SPARQLSearch(new SparqlEndpoint(url, Collections.<String>singletonList(defaultGraphURI), namedGraphURIs));
				} else {
					searchIndex = new VirtuosoSearch(new SparqlEndpoint(url, Collections.<String>singletonList(defaultGraphURI), namedGraphURIs));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return searchIndex;
	}
	
	public static void main(String[] args) {
		DatasetLoader.loadDatasets(DatasetLoader.class.getClassLoader().getResource("org/dllearner/autosparql/public/datasets.xml").getPath());
	}

}
