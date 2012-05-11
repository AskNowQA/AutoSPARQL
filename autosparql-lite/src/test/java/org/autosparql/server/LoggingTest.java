package org.autosparql.server;

import org.apache.log4j.Logger;
import org.junit.Test;

public class LoggingTest
{
	private static final Logger logger = Logger.getLogger(AutoSPARQLSession.class);
	
	@Test
	public void testLogging()
	{
		logger.error("logging test - error");
		logger.warn("logging test - warn");
		logger.info("logging test - info");
		logger.debug("logging test - debug");
		logger.trace("logging test - trace");
		
		AutoSPARQLSession.INSTANCE.equals(null);
	}
}
