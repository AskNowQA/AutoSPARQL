package org.autosparql.server;

import java.io.InputStream;
import java.util.Properties;

public final class Defaults
{
	private Defaults() {throw new AssertionError();}	

	private static final Properties properties = new Properties();
	static
	{		
		try{
			InputStream stream = Defaults.class.getResourceAsStream("autosparql-lite.properties");			
			properties.load(stream);
		}
		catch (Exception e) {throw new RuntimeException("Error loading properties. Cannot initialize Defaults.",e);}
	}

//	static boolean useDBpediaLive() {return Boolean.valueOf(properties.getProperty("useDBpediaLive"));}
	public static String endpointURL() {return properties.getProperty("endpointURL");}
	public static String graphURL() {return properties.getProperty("graphURL");}
	public static String solrServerURL() {return properties.getProperty("solrServerURL");}
	public static String sameAsServiceURL() {return properties.getProperty("sameAsServiceURL");}
}