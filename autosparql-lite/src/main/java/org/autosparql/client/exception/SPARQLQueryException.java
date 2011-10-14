package org.autosparql.client.exception;

import java.io.Serializable;

/**  Exception for when a SPARQL query goes wrong. Can be parameterized with query and endpoint which should always be done because it really facilitates bugfixing.
 * @author Konrad Höffner (original from Jens or Lorenz)*/
public class SPARQLQueryException extends AutoSPARQLException implements Serializable
{
	private static final long serialVersionUID = 3205559196686634580L;
	
	public final String query;
	public final String endpoint;
	
	public SPARQLQueryException(Exception e, String query,String endpoint)
	{
		super(e);
		this.query = query;
		this.endpoint = endpoint;		
	}

	public SPARQLQueryException(String query,String endpoint)
	{
		super();
		this.query = query;
		this.endpoint = endpoint;		
	}

	public SPARQLQueryException(String query)				{this(query,null);}
	public SPARQLQueryException(Exception e, String query)	{this(e,query,null);}
	

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if(query!=null||endpoint!=null)		{sb.append("Problem ");}
		if(query!=null)							{sb.append("with query \""	+query+'"');}
		if(endpoint!=null)						{sb.append("at endpont\""	+query+'"');}
		sb.append(super.toString());
		return sb.toString();
	}
}