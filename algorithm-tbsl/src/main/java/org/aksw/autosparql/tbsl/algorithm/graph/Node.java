package org.aksw.autosparql.tbsl.algorithm.graph;

import java.util.HashSet;
import java.util.Set;

public class Node {

	protected Set<String> types = new HashSet<String>();
	
	public Node() {
	
	}
	
	public void addType(String type){
		this.types.add(type);
	}
	

}
