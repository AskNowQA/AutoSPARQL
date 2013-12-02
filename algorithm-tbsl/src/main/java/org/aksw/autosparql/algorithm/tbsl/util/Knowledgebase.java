package org.aksw.autosparql.algorithm.tbsl.util;

import org.aksw.autosparql.commons.index.Indices;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;

public abstract class Knowledgebase {

	private String label;
	private String description;
	protected final Indices indices;
	
	public Indices getIndices() {return indices;}

	public Knowledgebase(String label, String description, Indices indices)
	{
		this.label = label;
		this.description = description;
		this.indices = indices;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
	
	@Override public String toString() {return label;}
	
	@Deprecated public Index getResourceIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}
	@Deprecated public Index getClassIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}	
	@Deprecated public Index getPropertyIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}
	@Deprecated public Index getObjectPropertyIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}
	@Deprecated public Index getDataPropertyIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}
	@Deprecated public MappingBasedIndex getMappingIndex() {throw new UnsupportedOperationException("knowledgebase was changed. refactor your code to use the new knowledgebase code");}
}