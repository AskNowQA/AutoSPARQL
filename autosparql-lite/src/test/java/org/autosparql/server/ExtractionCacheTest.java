package org.autosparql.server;

import static org.junit.Assert.assertTrue;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.autosparql.server.util.ExtractionDBCacheUtils;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.junit.Test;
import com.hp.hpl.jena.query.ResultSetRewindable;

public class ExtractionCacheTest
{
	@Test public void testJDBCMemory() throws SQLException {testJDBC("jdbc:h2:mem:");}		
	@Test public void testJDBCDiskRelative() throws SQLException {testJDBC("jdbc:h2:test");}
	@Test public void testJDBCDiskTempDir() throws SQLException {testJDBC("jdbc:h2:"+System.getProperty("java.io.tempdir"));}
	//@Test public void testTomcatWebappAutosparqlDirWithCacheSize100MB() throws SQLException {testJDBC("jdbc:h2:/usr/share/tomcat7/webapps/autosparql-lite/testcache/httplivedbpediaorgsparql_httpdbpediaorg;CACHE_SIZE=100000");}	 
	
	public void testJDBC(String url) throws SQLException
	{
			Connection conn;
			conn = DriverManager.getConnection(url, null, null);
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE TABLE IF NOT EXISTS TESTTABLE(TESTCOLUMN VARCHAR(100));");
			stmt.executeUpdate("INSERT INTO TESTTABLE VALUES ('TESTDATUM');");

			ResultSet rs = stmt.executeQuery("SELECT * FROM TESTTABLE;");
			rs.next();
			String datum = rs.getString(1);
			rs.close();
			conn.close();			
			assertTrue(datum.equals("TESTDATUM"));
	}
	
	@Test public void testExtractionDBCache() throws SQLException
	{		
		// "hack" get a memory connection. we shouldn't be able to write to etc so this would throw an error if it tries on-disk cache
		// TODO: if feature request gets implemented, use ExtractionDBCache(Connection)
		//ExtractionDBCacheUtils.setCacheDir("cache");
		ExtractionDBCache cache = ExtractionDBCacheUtils.getCache(SparqlEndpoint.getEndpointDBpedia().getURL().toString(),SparqlEndpoint.getEndpointDBpedia().getDefaultGraphURIs().get(0));
		String json = cache.executeSelectQuery(SparqlEndpoint.getEndpointDBpedia(),
				"select ?l {<http://dbpedia.org/resource/Leipzig> rdfs:label ?l. FILTER langmatches(lang(?l),'de').}");
		ResultSetRewindable rs = SparqlQuery.convertJSONtoResultSet(json);		
		String lexicalLabel = rs.next().getLiteral("?l").getLexicalForm();
		cache.closeConnection();
		assertTrue(lexicalLabel.equals("Leipzig"));
	}	
}