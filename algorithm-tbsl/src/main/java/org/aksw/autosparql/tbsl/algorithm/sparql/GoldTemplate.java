package org.aksw.autosparql.tbsl.algorithm.sparql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.tbsl.algorithm.util.StringDisplay;

public class GoldTemplate implements Serializable, Comparable<GoldTemplate> {

	private static final long serialVersionUID = -3925093269596915997L;

	com.hp.hpl.jena.query.Query query;
	List<Slot> slots;

	public GoldTemplate(com.hp.hpl.jena.query.Query q) {
		query = q;
		slots = new ArrayList<Slot>();
	}

	public com.hp.hpl.jena.query.Query getQuery() {
		return query;
	}

	public void setQuery(com.hp.hpl.jena.query.Query q) {
		query = q;
	}

	public void addSlot(Slot s) {
		slots.add(s);
	}

	public String toString() {
		String out = ">> QUERY:" + StringDisplay.shortenSparqlQuery(query.toString())+ " SLOTS: ";
		for (Slot s : slots) {
			out += s.toString() + "\n";
		}
		return out;
	}

	public List<Slot> getSlots() {
		return slots;
	}

	@Override
	public int compareTo(GoldTemplate o) {
		return getQuery().toString().compareTo(o.getQuery().toString());
	}

}
