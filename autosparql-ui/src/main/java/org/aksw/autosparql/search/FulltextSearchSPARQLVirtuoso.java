package org.aksw.autosparql.search;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.*;

import java.util.stream.Stream;

/**
 * Fulltext search optimized for Virtuoso using its built-in predicate bif:contains.
 *
 * @author Lorenz Buehmann
 */
public class FulltextSearchSPARQLVirtuoso implements FulltextSearch<SearchResultExtended> {

	private final QueryExecutionFactory qef;

	private ParameterizedSparqlString queryTemplate = new ParameterizedSparqlString(
			"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
					"prefix owl: <http://www.w3.org/2002/07/owl#> " +
					"select ?s (sample(?label) as ?l) (sample(?comment) as ?c) (GROUP_CONCAT(?type_label;separator=\", \") as ?types)\n" +
					"from <http://dbpedia.org> \n" +
					"from <http://dbpedia.org/labels> \n" +
					"from <http://dbpedia.org/comments> \n" +
					"where {\n" +
					"?s rdfs:label ?label . \n" +
					"?label <bif:contains> ?searchTerm .\n" +
					"?s a ?type .\n" +
					"FILTER NOT EXISTS {?s a ?sub_type. ?sub_type rdfs:subClassOf|owl:equivalentClass ?type FILTER(?sub_type != ?type)}\n" +
					"OPTIONAL{?type rdfs:label ?type_label . FILTER(LANGMATCHES(LANG(?type_label),'en'))}" +
					"?s rdfs:comment ?comment .\n" +
					"}\n" +
					"GROUP BY ?s\n" +
					"ORDER BY DESC(<LONG::IRI_RANK>(?s))\n" +
					"LIMIT 100");

	public FulltextSearchSPARQLVirtuoso(org.aksw.jena_sparql_api.core.QueryExecutionFactory qef) {
		this.qef = qef;
	}

	@Override
	public Stream<SearchResultExtended> search(String searchTerm) {
		queryTemplate.setLiteral("searchTerm", searchTerm);

		Query query = queryTemplate.asQuery();

		try (QueryExecution qe = qef.createQueryExecution(query)) {
			ResultSet rs = qe.execSelect();
			return ResultSetFormatter.toList(rs)
					.stream().map(qs -> new SearchResultExtended(qs.getResource("s"),
																			 qs.getLiteral("l").getLexicalForm(),
																			 qs.getLiteral("c").getLexicalForm(),
																			 qs.get("types").isResource()
																					 ? qs.getResource("types").getURI()
																					 : qs.getLiteral("types").getLexicalForm()
								  )
					);

		}
	}
}
