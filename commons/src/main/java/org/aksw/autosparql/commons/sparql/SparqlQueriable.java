package org.aksw.autosparql.commons.sparql;

import java.util.concurrent.TimeUnit;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.dllearner.kb.sparql.SparqlEndpoint;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public class SparqlQueriable
{
	protected QueryExecutionFactory qef;

	public SparqlQueriable(SparqlEndpoint endpoint, String cacheDirectory)
	{			
		qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		if(cacheDirectory != null){
				CacheFrontend frontend = CacheUtilsH2.createCacheFrontend("sparqlquerieable", false, TimeUnit.DAYS.toMillis(7));
				qef = new QueryExecutionFactoryCacheEx(qef, frontend);
		}
	}

	public SparqlQueriable(Model model)
	{
		qef = new QueryExecutionFactoryModel(model);
	}

	public ResultSet query(String query)
	{
		return qef.createQueryExecution(query).execSelect();
	}
}