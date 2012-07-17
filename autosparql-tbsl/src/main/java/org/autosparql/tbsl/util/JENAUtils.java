package org.autosparql.tbsl.util;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;

public class JENAUtils {
	
	public static Query writeOutPrefixes(Query query){
		return OpAsQuery.asQuery(Algebra.compile(query));
	}
	
	public static Query writeOutPrefixes(String queryString){
		return writeOutPrefixes(QueryFactory.create(queryString, Syntax.syntaxARQ));
	}

}
