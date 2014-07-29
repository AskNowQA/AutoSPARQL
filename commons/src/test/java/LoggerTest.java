import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.Test;

public class LoggerTest
{

	@Test public void testLogger()
	{
		Logger logger = Logger.getLogger(LoggerTest.class);
		assertNotNull(logger);
		logger.info("testing logger");
	}

}