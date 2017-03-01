package org.aksw.autosparql;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.AbstractDataProvider;
import com.vaadin.data.provider.CallbackDataProvider;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.Aggregator;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Lorenz Buehmann
 */
public class SPARQLBasedDataProvider extends AbstractBackEndDataProvider<RDFNode, Void> {

	private Query sparqlQuery;

	private final CallbackDataProvider.FetchCallback<RDFNode, Void> fetchCallback;
	private final CallbackDataProvider.CountCallback<RDFNode, Void> countCallback;

	private boolean orderByRelevance = true;

	public SPARQLBasedDataProvider(QueryExecutionFactory qef) {
		super();

		fetchCallback = new SPARQLFetchCallback(qef);
		countCallback = new SPARQLCountCallback(qef);
	}

	public void setQuery(Query query) {
		this.sparqlQuery = query;
	}

	public void setOrderByRelevance(boolean orderByRelevance) {
		this.orderByRelevance = orderByRelevance;
	}

	@Override
	protected Stream<RDFNode> fetchFromBackEnd(com.vaadin.data.provider.Query<RDFNode, Void> query) {
		return fetchCallback.fetch(query);
	}

	@Override
	protected int sizeInBackEnd(com.vaadin.data.provider.Query<RDFNode, Void> query) {
		return countCallback.count(query);
	}

	class SPARQLCountCallback implements CallbackDataProvider.CountCallback<RDFNode, Void> {

		QueryExecutionFactory qef;

		SPARQLCountCallback(QueryExecutionFactory qef) {
			this.qef = qef;
		}

		@Override
		public int count(com.vaadin.data.provider.Query<RDFNode, Void> query) {
			if(sparqlQuery == null) {
				return 0;
			}

			Query copy = sparqlQuery.cloneQuery();
			int cnt = 0;
			Var vars = copy.getProject().getVars().iterator().next();
			copy.setDistinct(false);
			copy.getProject().clear();
			Aggregator aggregator = AggregatorFactory.createCountExpr(true,
																	  new ExprVar(vars));
			Expr expr = copy.allocAggregate(aggregator);
			copy.addResultVar(Var.alloc("cnt"), expr);

			try(QueryExecution qe = qef.createQueryExecution(copy)) {
				ResultSet rs = qe.execSelect();
				if(rs.hasNext()) {
					cnt = rs.next().getLiteral("cnt").getInt();
				}
			}
			return cnt;
		}
	}

	class SPARQLFetchCallback implements CallbackDataProvider.FetchCallback<RDFNode, Void> {

		QueryExecutionFactory qef;

		SPARQLFetchCallback(QueryExecutionFactory qef) {
			this.qef = qef;
		}

		@Override
		public Stream<RDFNode> fetch(com.vaadin.data.provider.Query<RDFNode, Void> query) {
			if(sparqlQuery == null) {
				return Stream.empty();
			}

			Query copy = sparqlQuery.cloneQuery();
			// The index of the first item to load
			int offset = query.getOffset();

			// The number of items to load
			int limit = query.getLimit();

			copy.setLimit(limit);
			copy.setOffset(offset);

			// ORDER BY DESC ( <LONG::IRI_RANK> (?s) )
			if(orderByRelevance) {
				copy.addOrderBy(new E_IRI_Rank(new ExprVar("s")), Query.ORDER_DESCENDING);
			}

			List<RDFNode> res = new ArrayList<>();

			try(QueryExecution qe = qef.createQueryExecution(copy)) {
				ResultSet rs = qe.execSelect();
				while(rs.hasNext()) {
					QuerySolution qs = rs.next();
					res.add(qs.getResource("s"));
				}
			}
			return res.stream();
		}
	}

//	public static <T, F> SPARQLBasedDataProvider<T, F> create(QueryExecutionFactory qef) {
//		return new SPARQLBasedDataProvider<T, F>(qef,
//
//	}
}
