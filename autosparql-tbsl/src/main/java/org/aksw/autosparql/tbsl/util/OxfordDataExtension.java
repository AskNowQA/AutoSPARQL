package org.aksw.autosparql.tbsl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.autosparql.commons.diadem.Word;
import org.aksw.autosparql.commons.diadem.WordFrequencyCounter;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.stanford.nlp.util.StringUtils;

/** will be moved to org.aksw.autosparql.algorithm.tbsl.knowledgebase*/
@Deprecated
public class OxfordDataExtension {
	
	private static final String featurePropertyURI = "http://diadem.cs.ox.ac.uk/ontologies/real-estate#feature";
	private static final List<String> features = Arrays.asList(new String[]{"garden", "balcony", "garage", "central heating", "shower", "pool"});
	
	public static void main(String[] args) throws Exception{
		String decriptionPropertyURI = "http://purl.org/goodrelations/v1#description";
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
		ExtractionDBCache cache = new ExtractionDBCache("cache");
		LuceneIndex index = new LuceneIndex("/opt/tbsl/oxford_index");
		
		//load the descriptions
		Map<String, String> uri2Description = new HashMap<String, String>();
		String query = String.format("SELECT ?uri ?desc WHERE {?uri <%s> ?desc}", decriptionPropertyURI);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			String uri = qs.get("uri").asResource().getURI();
			String description = qs.get("desc").asLiteral().getLexicalForm();
			uri2Description.put(uri, description);
		}
		
		//extract the most frequent terms used in the descriptions
		String allDescriptions = StringUtils.join(uri2Description.values(), " ");
		WordFrequencyCounter wfc = new WordFrequencyCounter();
		for (Word word : wfc.getKeywordsSortedByFrequency(allDescriptions)) {
			System.out.println(word.getWord() + ":\t" + word.getFrequency());
		}
		
		//generate triples for each feature
		Model model = ModelFactory.createDefaultModel();
		Property featureProperty = model.createProperty(featurePropertyURI);
		for(String feature : features){
			System.out.println("Searching for entries with feature \"" + feature + "\"...");
			query = String.format("SELECT DISTINCT ?uri WHERE {?uri <%s> ?desc. ?desc bif:contains '\"%s\"'}", decriptionPropertyURI, feature);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			int cnt = 0;
			while(rs.hasNext()){
				qs = rs.next();
				Resource resource = qs.get("uri").asResource();
				model.add(resource, featureProperty, feature);
				cnt++;
			}
			System.out.println("...found " + cnt + " entries.");
		}
		model.write(new FileOutputStream(new File("/opt/tbsl/oxford_features.ttl")), "TURTLE");
	}

}
