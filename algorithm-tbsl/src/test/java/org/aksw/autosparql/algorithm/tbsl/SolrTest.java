package org.aksw.autosparql.algorithm.tbsl;
import org.dllearner.common.index.SOLRIndex;
import org.junit.Test;

public class SolrTest
{
//	private static final String SOLR_SERVER_URI_EN	= "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_";
	private static final String SOLR_SERVER_URI_EN	= "http://solr.aksw.org/en_";

	@Test public void solrTest()	
	{
		SOLRIndex index = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_object_properties","label");
		System.out.println("written: "+index.getResources("written"));
		SOLRIndex index2 = new SOLRIndex(SOLR_SERVER_URI_EN+"boa","nlr-no-var");
		index2.setSortField("boa-score");
		System.out.println("written: "+index2.getResources("written"));

	}
}