package org.aksw.autosparql.algorithm.tbsl.graph;

import java.util.HashSet;
import java.util.Set;
import org.aksw.autosparql.algorithm.tbsl.sparql.Slot;

public class TemplateNode {

	protected Set<Slot> typeSlots = new HashSet<Slot>();

	private Slot slot;
	private String varName;

	public TemplateNode(Slot slot) {
		this.slot = slot;
		this.varName = slot.getAnchor();
	}

	public TemplateNode(String varName) {
		this.varName = varName;
	}

	public Slot getSlot() {
		return slot;
	}

	public String getVarName() {
		return varName;
	}

	public void addType(Slot typeSlot) {
		this.typeSlots.add(typeSlot);
	}

	public Set<Slot> getTypeSlots() {
		return typeSlots;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append((slot != null) ? slot.getWords().toString() : varName);
		if (!typeSlots.isEmpty()) {
			sb.append("{types=");
			for (Slot slot : typeSlots) {
				sb.append(slot.getWords().toString());
			}
			sb.append("}");
		}
		return sb.toString();
	}
}
