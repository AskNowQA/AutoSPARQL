package org.aksw.autosparql.commons.index;
/*
package org.aksw.autosparql.tbsl.algorithm.util;

import org.aksw.autosparql.commons.index.Indices;
import org.aksw.autosparql.tbsl.algorithm.search.BugfixedSolrIndex;
import org.aksw.autosparql.tbsl.algorithm.search.DbpediaFilter;
import org.aksw.autosparql.tbsl.algorithm.search.FilteredIndex;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;

//** @author konrad
// Capsules the necessary settings for the AKSW solr server settings
public enum SolrServerAksw
{
	INSTANCE;

//	static final String	SOLR_SERVER_URI_EN	= "http://solr.aksw.org/en_";
	static final String	SOLR_SERVER_URI_EN	= "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_";

	public final Index resourcesIndex;
	public final Index classesIndex;
	public final Index dataPropertiesIndex;
	public final Index objectPropertiesIndex;

	public final Indices dbpediaIndices;

//	// boa index already integrated
//	private SolrServerAksw()
//	{
//		resourcesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");
//		classesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_classes");
//		dataPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
//		objectPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_object_properties");
//		for(BugfixedSolrIndex index: new BugfixedSolrIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
//		{index.setPrimarySearchField("label");}
//		dbpediaIndices = new Indices(resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex);
//	}

// separate boa index
	private SolrServerAksw()
	{
		BugfixedSolrIndex resourcesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");
		BugfixedSolrIndex classesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_classes");
		BugfixedSolrIndex dataPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		BugfixedSolrIndex objectPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		for(BugfixedSolrIndex index: new BugfixedSolrIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
		{index.setPrimarySearchField("label");}
		BugfixedSolrIndex boaIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"boa","nlr-no-var");
		boaIndex.setSortField("boa-score");
		boaIndex.getResources("test");

		this.resourcesIndex = new FilteredIndex(resourcesIndex, DbpediaFilter.INSTANCE);
		this.classesIndex = new FilteredIndex(classesIndex, DbpediaFilter.INSTANCE);
		this.dataPropertiesIndex= new FilteredIndex(new HierarchicalIndex(dataPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);
		this.objectPropertiesIndex = new FilteredIndex(new HierarchicalIndex(objectPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);

		dbpediaIndices = new Indices(resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex);
	}

}
*/