package org.aksw.autosparql.server.util;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.autosparql.server.Defaults;
import org.aksw.autosparql.shared.ResourceImageLinks;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;

/** 
 */
public final class SameAsLinks
{
	private SameAsLinks() {throw new AssertionError();}
	
	/** Uses web services to get resources r where resourceURI owl:sameAs r holds.
	 * Gets at most one link for each of the predetermined prefixes where there is an icon for the links and gets no links for other prefixes.    
	 * The web service uri is configured in autosparql-lite.properties.
	 * @param resourceURI the uri for which to find sameAs links
	 * @return a list of resources r where resourceURI owl:sameAs r holds.
	 */
	public static List<String> getSameAsLinksForShowing(String resourceURI)
	{
		List<String> sameAsLinks = new ArrayList<String>();
		try
		{
			// service url already shaped so that the resource can just be added 
			String requestURI = Defaults.sameAsServiceURL() + URLEncoder.encode(resourceURI, "UTF-8");
			URLConnection conn = new URL(requestURI).openConnection();
			Model model = ModelFactory.createDefaultModel();
			model.read(conn.getInputStream(), null);
			String url;
			Set<String> used = new HashSet<String>(); // each prefix at most once

			for(Statement st : model.listStatements(null, OWL.sameAs, (RDFNode)null).toList()) // cast because we don't want labels as objects
			{
				url = st.getObject().asResource().getURI();
				String prefix;

				// has an image which is not shown before?
				if((prefix = ResourceImageLinks.prefix(url)) != null && used.add(prefix)) {sameAsLinks.add(url);}
			}
			// add a link from DBpedia-Wikipedia
			if(resourceURI.startsWith("http://dbpedia.org/resource/")) sameAsLinks.add(resourceURI.replace("http://dbpedia.org/resource/", "http://en.wikipedia.org/wiki/"));
		} catch (UnsupportedEncodingException e) {throw new RuntimeException("UTF-8");}
		catch (Exception e) {throw new RuntimeException("Failed getting sameAs-links for resource URI \""+resourceURI+"\".",e);}
		return sameAsLinks;
	}

}