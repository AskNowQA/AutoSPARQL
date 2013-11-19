package org.aksw.autosparql.algorithm.tbsl.graph;

import org.jgrapht.graph.DefaultEdge;

public class PropertyEdge extends DefaultEdge {
	private Node subject;
	private Node object;
	private String label;

	public PropertyEdge(Node subject, Node object, String label) {
		this.subject = subject;
		this.object = object;
		this.label = label;
	}

	public Node getSubject() {
		return subject;
	}

	public Node getObject() {
		return object;
	}

	public String toString() {
		return label;
	}

}
