package org.aksw.autosparql.commons.index;

import java.util.Arrays;
import java.util.Collection;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;
import com.hp.hpl.jena.rdf.model.Model;

public class Indices
{
	public final Index resourceIndex;
	public final Index classIndex;
	public final Index objectPropertyIndex;
	public final Index dataPropertyIndex;
	public final MappingBasedIndex mappingIndex;

	public Collection<Index> getIndices() {	return Arrays.asList(new Index[]{resourceIndex,classIndex,objectPropertyIndex,dataPropertyIndex});}
	
	public Indices(Index resourceIndex, Index classIndex, Index objectPropertyIndex, Index dataPropertyIndex)
	{this(resourceIndex, classIndex, objectPropertyIndex, dataPropertyIndex,null);}
	
	public Indices(Index resourceIndex, Index classIndex, Index objectPropertyIndex, Index dataPropertyIndex,MappingBasedIndex mappingIndex)
	{
		super();
		this.resourceIndex = resourceIndex;
		this.classIndex = classIndex;
		this.objectPropertyIndex = objectPropertyIndex;
		this.dataPropertyIndex = dataPropertyIndex;
		this.mappingIndex=mappingIndex;
	}

	public Index getResourceIndex() {return resourceIndex;}
	public Index getClassIndex() {return classIndex;}	
	public Index getObjectPropertyIndex() {return objectPropertyIndex;}
	public Index getDataPropertyIndex() {return dataPropertyIndex;}
	public MappingBasedIndex getMappingIndex() 	{return mappingIndex;}
//	public Index getPropertyIndex()
//	{
//		if(propertyIndex==null) propertyIndex = new HierarchicalIndex(objectPropertyIndex,dataPropertyIndex); 
//		return propertyIndex;
//	}
//	public MappingBasedIndex getMappingIndex() {return mappingIndex;}

	@Deprecated public Index getPropertyIndex()
	{
		throw new UnsupportedOperationException("use get datapropertyindex or objectpropertyindex");
//		return null;
	}
	
	public Indices(SparqlEndpoint endpoint)
	 {
		 this(new SPARQLIndex(endpoint), new SPARQLClassesIndex(endpoint), new SPARQLObjectPropertiesIndex(endpoint), new SPARQLDatatypePropertiesIndex(endpoint));
	 }

	 public Indices(Model model)
	 {
		 this(new SPARQLIndex(model), new SPARQLClassesIndex(model), new SPARQLObjectPropertiesIndex(model), new SPARQLDatatypePropertiesIndex(model));
	 }
}