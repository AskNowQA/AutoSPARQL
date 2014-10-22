package org.aksw.autosparql.tbsl.algorithm.graph;

public class EntityNode extends Node{

	private String token;

	public EntityNode(String token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return token + (types.isEmpty() ? "" : types.toString());
	}
}
