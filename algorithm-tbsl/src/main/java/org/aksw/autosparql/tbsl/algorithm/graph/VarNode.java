package org.aksw.autosparql.tbsl.algorithm.graph;

public class VarNode extends Node{

	private String name;

	public VarNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + (types.isEmpty() ? "" : types.toString());
	}

}
