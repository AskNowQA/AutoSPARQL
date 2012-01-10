package org.autosparql.shared;

@SuppressWarnings("serial")
public class SPARQLException extends RuntimeException
{
	public SPARQLException(Throwable cause, String query, String endpoint)
	{
		super("Error with query "+query+" at endpoint "+endpoint,cause);
	}
}
