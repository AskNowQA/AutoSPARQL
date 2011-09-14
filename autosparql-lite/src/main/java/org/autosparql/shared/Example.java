package org.autosparql.shared;

import com.extjs.gxt.ui.client.data.BaseModel;

/** Can hold more than just the properties with the get-methods (should include all triples for the resource).
 * @author originally by Lorenz Bühmann, extended by Konrad Höffner */
public class Example extends BaseModel {

	private static final long serialVersionUID = 6955538657940009581L;

	/** sorl is used as a fallback if the normal endpoint does not work */
	public boolean containsSolrData = false;
	
	public Example(){}
	
	@Override public int hashCode()
	{
		return get("uri").hashCode();
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
	
	public Example(String uri, String label, String imageURL, String comment)
	{
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
