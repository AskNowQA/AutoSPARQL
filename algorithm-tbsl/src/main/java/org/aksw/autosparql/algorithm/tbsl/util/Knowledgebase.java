package org.aksw.autosparql.algorithm.tbsl.util;

import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;

public abstract class Knowledgebase {

	private String label;
	private String description;

	private Index resourceIndex;
	private Index objectPropertyIndex;
	private Index dataPropertyIndex;
	
	private Index propertyIndex;
	
	private Index classIndex;
	
	private MappingBasedIndex mappingIndex;

	public Knowledgebase(String label, String description,
			Index resourceIndex, Index objectPropertyIndex, Index dataPropertyIndex, Index classIndex) {
		this(label, description, resourceIndex, objectPropertyIndex, dataPropertyIndex, classIndex, null);
	}
	
	public Knowledgebase(String label, String description,
			Index resourceIndex, Index objectPropertyIndex, Index dataPropertyIndex, Index classIndex, MappingBasedIndex mappingIndex) {
		this.label = label;
		this.description = description;
		this.resourceIndex = resourceIndex;
		this.objectPropertyIndex = objectPropertyIndex;
		this.dataPropertyIndex = dataPropertyIndex;
		this.classIndex = classIndex;
		this.mappingIndex = mappingIndex;
	}
	
	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public Index getResourceIndex() {
		return resourceIndex;
	}

	public Index getObjectPropertyIndex() {
		return objectPropertyIndex;
	}

	public Index getDataPropertyIndex() {
		return dataPropertyIndex;
	}
	
	public Index getPropertyIndex()
	{
		if(propertyIndex==null) propertyIndex = new HierarchicalIndex(objectPropertyIndex,dataPropertyIndex); 
		return propertyIndex;
	}


	public Index getClassIndex() {
		return classIndex;
	}
	
	public MappingBasedIndex getMappingIndex() {
		return mappingIndex;
	}

	@Override
	public String toString() {
		return label;
	}

}
