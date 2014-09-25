package org.aksw.autosparql.commons.index;

import org.aksw.autosparql.commons.search.DbpediaFilter;
import org.aksw.autosparql.commons.search.FilteredIndex;
import org.aksw.rdfindex.HierarchicalIndex;
import org.aksw.rdfindex.Index;
import org.aksw.rdfindex.Indices;
import org.aksw.rdfindex.SOLRIndex;

/** Capsules the necessary settings for the AKSW SOLR server settings
 *   @author konrad */
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
//		resourcesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");		
//		classesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_classes");
//		dataPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
//		objectPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_object_properties");
//		for(SOLRIndex index: new SOLRIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
//		{index.setPrimarySearchField("label");}
//		dbpediaIndices = new Indices(resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex);
//	}

// separate boa index
	private SolrServer()
	{
		SOLRIndex resourcesIndex = new SOLRIndex(SOLR_SERVER_URI_EN_DBPEDIA_RESOURCES);
		SOLRIndex classesIndex = new SOLRIndex(SOLR_SERVER_URI_EN_DBPEDIA_CLASSES);
		SOLRIndex dataPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN_DBPEDIA_DATA_PROPERTIES);
		SOLRIndex objectPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN_DBPEDIA_OBJECT_PROPERTIES);
		for(SOLRIndex index: new SOLRIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
		{index.setPrimarySearchField("label");}
		SOLRIndex boaIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"boa","nlr-no-var");
		boaIndex.setSortField("boa-score");
//		boaIndex.getResources("test");
		
		this.resourcesIndex = new FilteredIndex(resourcesIndex, DbpediaFilter.INSTANCE);
		this.classesIndex = new FilteredIndex(classesIndex, DbpediaFilter.INSTANCE);
		this.dataPropertiesIndex= new FilteredIndex(new HierarchicalIndex(dataPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);
		this.objectPropertiesIndex = new FilteredIndex(new HierarchicalIndex(objectPropertiesIndex,boaIndex),DbpediaFilter.INSTANCE);

		dbpediaIndices = new Indices(this.resourcesIndex,this.classesIndex,this.objectPropertiesIndex,this.dataPropertiesIndex);
	}
	
}