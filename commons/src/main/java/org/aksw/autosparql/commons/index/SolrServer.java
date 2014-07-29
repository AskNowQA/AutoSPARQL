package org.aksw.autosparql.commons.index;

import org.aksw.autosparql.commons.search.DbpediaFilter;
import org.aksw.autosparql.commons.search.FilteredIndex;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;

/** @author konrad
 * Capsules the necessary settings for the AKSW solr server settings
*/
public enum SolrServer
{
	INSTANCE;

	public Indices getIndices() {return dbpediaIndices;}
	
	static public final String	SOLR_SERVER_URI_EN	= "http://linkedspending.aksw.org/solr/en_";
	static public final String SOLR_SERVER_URI_EN_DBPEDIA_RESOURCES = SOLR_SERVER_URI_EN+"dbpedia_resources";
	static public final String SOLR_SERVER_URI_EN_DBPEDIA_CLASSES = SOLR_SERVER_URI_EN+"dbpedia_classes";
	static public final String SOLR_SERVER_URI_EN_DBPEDIA_DATA_PROPERTIES = SOLR_SERVER_URI_EN+"dbpedia_data_properties";
	static public final String SOLR_SERVER_URI_EN_DBPEDIA_OBJECT_PROPERTIES = SOLR_SERVER_URI_EN+"dbpedia_object_properties";
//	static final String	BOA_SERVER_URI_EN	= "http://[2001:638:902:2010:0:168:35:138]:8080/solr/boa";
	
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
	private SolrServer()
	{
		BugfixedSolrIndex resourcesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN_DBPEDIA_RESOURCES);
		BugfixedSolrIndex classesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN_DBPEDIA_CLASSES);
		BugfixedSolrIndex dataPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN_DBPEDIA_DATA_PROPERTIES);
		BugfixedSolrIndex objectPropertiesIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN_DBPEDIA_OBJECT_PROPERTIES);
		for(BugfixedSolrIndex index: new BugfixedSolrIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
		{index.setPrimarySearchField("label");}
		BugfixedSolrIndex boaIndex = new BugfixedSolrIndex(SOLR_SERVER_URI_EN+"boa","nlr-no-var");
		boaIndex.setSortField("boa-score");
//		boaIndex.getResources("test");
		
		this.resourcesIndex = new FilteredIndex(resourcesIndex, DbpediaFilter.INSTANCE);
		this.classesIndex = new FilteredIndex(classesIndex, DbpediaFilter.INSTANCE);
		this.dataPropertiesIndex= new FilteredIndex(new HierarchicalIndex(dataPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);
		this.objectPropertiesIndex = new FilteredIndex(new HierarchicalIndex(objectPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);

		dbpediaIndices = new Indices(this.resourcesIndex,this.classesIndex,this.objectPropertiesIndex,this.dataPropertiesIndex);
	}
	
}