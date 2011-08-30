package org.autosparql.shared;

import com.extjs.gxt.ui.client.data.BaseModel;

public class Example extends BaseModel {

	private static final long serialVersionUID = 6955538657940009581L;
	
	public Example(){
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o!=null&&o instanceof Example) return this.equals((Example)o);
		return false;
	}
	
	public boolean equals(Example e)
	{
		return get("uri").toString().equals(e.get("uri").toString());
	}
	
	public Example(String uri, String label, String imageURL, String comment){
		set("uri", uri);
		set("label", label);
		set("imageURL", imageURL);
		set("comment", comment);
	}
	
	public String getURI(){
		return (String)get("uri");
	}
	
	public String getLabel(){
		return (String)get("label");
	}
	
	public String getImageURL(){
		return (String)get("imageURL");
	}
	
	public String getComment(){
		return (String)get("comment");
	}
	
	@Override
	public String toString() {
		return getURI();
	}

}
