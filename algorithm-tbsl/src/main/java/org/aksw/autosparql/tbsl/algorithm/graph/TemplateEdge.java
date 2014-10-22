package org.aksw.autosparql.tbsl.algorithm.graph;

import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.jgrapht.graph.DefaultEdge;

public class TemplateEdge extends DefaultEdge {

	private Slot propertySlot;

	public TemplateEdge(Slot propertySlot) {
		this.propertySlot = propertySlot;
	}

	public String toString() {
		return propertySlot.getWords().toString();
	}

}
