package org.aksw.autosparql;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Virtuoso IRI_RANK function.
 *
 * @author Lorenz Buehmann
 */
public class E_IRI_Rank extends ExprFunction1 {

	private static final String symbol = "<LONG::IRI_RANK>";

	public E_IRI_Rank(Expr expr) {
		super(expr, symbol) ;
	}

	@Override
	public NodeValue eval(NodeValue v) {
		return null;
	}

	@Override
	public Expr copy(Expr expr) {
		return new E_IRI_Rank(expr);
	}
}
