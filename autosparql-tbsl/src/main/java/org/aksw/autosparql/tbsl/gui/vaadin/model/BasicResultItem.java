package org.aksw.autosparql.tbsl.gui.vaadin.model;

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
		// workaround for faulty dbpedia urls
		if(imageURL!=null) {imageURL=imageURL.replace("http://upload.wikimedia.org/wikipedia/commons/","http://upload.wikimedia.org/wikipedia/en/");}
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

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		BasicResultItem other = (BasicResultItem) obj;
//		if (uri == null) {
//			if (other.uri != null)
//				return false;
//		} else if (!uri.equals(other.uri))
//			return false;
//		return true;
//	}



}
