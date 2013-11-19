package org.aksw.autosparql.server.util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.aksw.autosparql.server.util.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;

/** utilitymethod for loading endpoints from specific configuration files. */
public final class Endpoints
{	
	static private Logger log = Logger.getLogger(Endpoints.class.toString());
	private Endpoints() {throw new AssertionError();}	
	
	public static List<SPARQLEndpointEx> loadEndpoints(InputStream stream) throws ConfigurationException
	{
		
		List<SPARQLEndpointEx> endpoints = new ArrayList<SPARQLEndpointEx>();

			XMLConfiguration config = new XMLConfiguration();
			config.load(stream);
			List<String> endpointConfigurations = CollectionUtils.toStringList(config.configurationsAt("endpoint"));
			for(Iterator<?> iter = endpointConfigurations.iterator();iter.hasNext();){
				HierarchicalConfiguration endpointConf = (HierarchicalConfiguration) iter.next();
				endpoints.add(createEndpoint(endpointConf));
			}
		return endpoints;
	}

	private static SPARQLEndpointEx createEndpoint(HierarchicalConfiguration endpointConf){
		try {
			URL url = new URL(endpointConf.getString("url"));
			String label = endpointConf.getString("label");
			String prefix = endpointConf.getString("prefix");
			if(prefix == null){
				prefix = label.replaceAll("@", "").replaceAll(" ", "");
			}
			String defaultGraphURI = endpointConf.getString("defaultGraphURI");
			List<String> namedGraphURIs = CollectionUtils.toStringList(endpointConf.getList("namedGraphURI"));
			List<String> predicateFilters = CollectionUtils.toStringList(endpointConf.getList("predicateFilters.predicate"));

			return new SPARQLEndpointEx(url, Collections.singletonList(defaultGraphURI), namedGraphURIs, label, prefix, new HashSet<String>(predicateFilters));
		} catch (MalformedURLException e)
		{
			log.warning("Could not parse URL from SPARQL endpoint.");
			e.printStackTrace();
		}
		return null;	
	}

}