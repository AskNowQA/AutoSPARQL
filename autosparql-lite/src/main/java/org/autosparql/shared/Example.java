package org.autosparql.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.core.FastMap;
import com.extjs.gxt.ui.client.core.FastSet;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.NestedModelUtil;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/** Can hold more than just the properties with the get-methods (should include all triples for the resource).
 * @author originally by Lorenz Bühmann, extended by Konrad Höffner */
public class Example extends BaseModel
{
	private static final long serialVersionUID = 6955538657940009581L;

	public final static String LABEL = "http://www.w3.org/1999/02/22-rdf-syntax-ns#label";
	public final static String IMAGE_URL = "http://xmlns.com/foaf/0.1/depiction";
	public final static String COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";

	// else there are problems with dots in the names
	protected boolean allowNestedValues = false;

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
		set(IMAGE_URL, imageURL);
		set(COMMENT, comment);
	}

	// dots trigger nested properties which we don't want, thus escape them
	protected static final String ESCAPED_DOT = "###DOT###";

	@Override
	public <X> X get(String property) {
		return super.get(property.replace(".", ESCAPED_DOT));
	}

	@Override
	public <X> X set(String name, X value)
	{
		name = name.replace(".", ESCAPED_DOT);
		X oldValue = super.set(name, value);
		notifyPropertyChanged(name, value, oldValue);
		return oldValue;
	}
	// --------------------------------------------------

	public String getURI(){
		return (String)get("uri");
	}	


	public String getLabel(){
		return (String)get(LABEL);
	}

	public String getImageURL(){
		return (String)get(IMAGE_URL);
	}

	public String getComment(){
		return (String)get(COMMENT);
	}

	@Override
	public Collection<String> getPropertyNames() {
		Set<String> set = new FastSet();
		if (map != null) {
			for(String key: map.keySet())
			set.add(key.replace(ESCAPED_DOT,"."));
		}
		return set;
	}
	
	  public Map<String, Object> getProperties() {
		    Map<String, Object> newMap = new FastMap<Object>();
		    if (map != null) {
				for(String key: map.keySet()) {newMap.put(key.replace(ESCAPED_DOT,"."),map.get(key));}
		      newMap.putAll(map.getTransientMap());
		    }
		    return newMap;
		  }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(String name: this.getPropertyNames())
		{
			sb.append(name+"->"+this.getProperties().get(name));
			sb.append(',');
		}
		return sb.substring(0,sb.length()-1);
	}

}