package org.aksw.autosparql.tbsl.gui.vaadin.model;

import java.util.List;
import java.util.Map;

public class SelectAnswer implements Answer{
	
	private List<BasicResultItem> items;
	private Map<String, Integer> additionalProperties;
	private List<String> prominentProperties;
	
	public SelectAnswer(List<BasicResultItem> items, List<String> prominentProperties, Map<String, Integer> additionalProperties) {
		this.items = items;
		this.prominentProperties = prominentProperties;
		this.additionalProperties = additionalProperties;
	}
	
	public List<BasicResultItem> getItems() {
		return items;
	}
	
	public Map<String, Integer> getAdditionalProperties() {
		return additionalProperties;
	}
	
	public List<String> getProminentProperties() {
		return prominentProperties;
	}
	
	@Override
	public boolean isBoolean() {
		return false;
	}

}
