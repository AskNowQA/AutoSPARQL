package org.aksw.autosparql.server.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.aksw.autosparql.client.model.Example;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class ExampleBuilder {
	
	private ExtractionDBCache cache;
	private SparqlEndpoint endpoint;
	
	protected String exampleQueryTemplate = "SELECT ?label ?imageURL ?comment WHERE {\n" +
	"<uri> <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
	"FILTER(LANGMATCHES(LANG(?label), \"en\"))\n" +
	"OPTIONAL{<uri> <http://dbpedia.org/ontology/thumbnail> ?imageURL.}\n" + 
	"OPTIONAL{<uri> <http://www.w3.org/2000/01/rdf-schema#comment> ?comment. FILTER(LANGMATCHES(LANG(?comment), \"en\"))}\n" +
	"} ";
	
	public ExampleBuilder(ExtractionDBCache cache, SparqlEndpoint endpoint) {
		this.cache = cache;
		this.endpoint = endpoint;
	}

	public Example buildExample(String uri){
		String query = exampleQueryTemplate.replaceAll("uri", uri);
			
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		String label = "";
		String imageURL = "";
		String comment = "";
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			if(qs.getLiteral("label") != null){
				label = qs.getLiteral("label").getLexicalForm();
			}
			if(qs.getResource("imageURL") != null){
				imageURL = qs.getResource("imageURL").getURI();
			}
			if(qs.getLiteral("comment") != null){
				comment = qs.getLiteral("comment").getLexicalForm();
			}
			
			return new Example(uri, label, imageURL, comment);
		}
		return null;
	}
	
	public Set<Example> buildExamples(Collection<String> uris){
		Set<Example> examples = new HashSet<Example>();
		for(String uri : uris){
			examples.add(buildExample(uri));
		}
		return examples;
	}
	
}
