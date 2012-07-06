package org.autosparql.tbsl.model;

import java.util.Map;

public class BasicResultItem implements ResultItem{
	
	private String uri;
	private String label;
	private String description;
	private String imageURL;
	
	private Map<String, Object> data;
	
	public BasicResultItem(String uri, String label, String description) {
		this(uri, label, description, null);
	}
	
	public BasicResultItem(String uri, String label, String description, String imageURL) {
		this(uri, label, description, imageURL, null);
	}
	
	public BasicResultItem(String uri, String label, String description, String imageURL, Map<String, Object> data) {
		this.uri = uri;
		this.label = label;
		this.description = description;
		this.imageURL = imageURL;
		this.data = data;
	}
	
	public String getUri() {
		return uri;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
	
	public Map<String, Object> getData() {
		return data;
	}

	public String getImageURL() {
		return imageURL;
	}
	
	public Object getValue(String property){
		return data.get(property);
	}
	
	
}
