package org.aksw.autosparql.algorithm.tbsl.util;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;

public enum SolrServerAksw
{
	INSTANCE;
	
	static final String	SOLR_SERVER_URI_EN	= "http://solr.aksw.org/solr/en_";

	public final SOLRIndex resourcesIndex;		
	public final SOLRIndex classesIndex;
	public final SOLRIndex dataPropertiesIndex;
	public final SOLRIndex objectPropertiesIndex;
	
	public final Indices dbpediaIndices;  
	
	private SolrServerAksw()
	{
		resourcesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");		
		classesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_classes");
		dataPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		objectPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		for(SOLRIndex index: new SOLRIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
		{index.setPrimarySearchField("label");}
		SOLRIndex boaIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"boa","nlr-no-var");		
		Index newDataPropertiesIndex = new HierarchicalIndex(dataPropertiesIndex,boaIndex);
		Index newObjectPropertiesIndex = new HierarchicalIndex(objectPropertiesIndex,boaIndex);
		dbpediaIndices = new Indices(resourcesIndex,classesIndex,newObjectPropertiesIndex,newDataPropertiesIndex);
	}
	
}