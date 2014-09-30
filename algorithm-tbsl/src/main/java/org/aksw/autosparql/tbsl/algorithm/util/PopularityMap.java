package org.aksw.autosparql.tbsl.algorithm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.aksw.autosparql.commons.sparql.SparqlQueriable;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;
import com.clarkparsia.owlapiv3.OWL;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/** calculates for each class how many instances it has, and for each property, in how many triples it is used.
 * for individuals it counts outgoing or ingoing links (check todo)**/
public class PopularityMap {

	public enum EntityType {
		CLASS, PROPERTY, RESOURCE
	}

	private String file;

	private Map<String, Integer> class2Popularity = new HashMap<String, Integer>();
	private Map<String, Integer> property2Popularity = new HashMap<String, Integer>();
	private Map<String, Integer> resource2Popularity = new HashMap<String, Integer>();

	final SparqlQueriable queryable;

	public PopularityMap(String file, SparqlQueriable queryable)
	{
		this.file = file;
		this.queryable=queryable;

		boolean deserialized = deserialize();
		if(!deserialized){
			// load popularity of classes
			for (NamedClass nc : getAllClasses()) {
				System.out.println("Computing popularity for " + nc);
				int popularity = loadPopularity(nc.getName(), EntityType.CLASS);
				class2Popularity.put(nc.getName(), Integer.valueOf(popularity));
			}
			// load popularity of properties
			for (ObjectProperty op : getAllObjectProperties()) {
				System.out.println("Computing popularity for " + op);
				int popularity = loadPopularity(op.getName(), EntityType.PROPERTY);
				property2Popularity.put(op.getName(), Integer.valueOf(popularity));
			}
			for (DatatypeProperty dp : getAllDataProperties()) {
				System.out.println("Computing popularity for " + dp);
				int popularity = loadPopularity(dp.getName(), EntityType.PROPERTY);
				property2Popularity.put(dp.getName(), Integer.valueOf(popularity));
			}
			serialize();
		}
	}

	/* adapted from SPARQLTasks which doesn't accept models *****************************************************************************/

	public Set<NamedClass> getAllClasses() {
		Set<NamedClass> classes = new TreeSet<NamedClass>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?c WHERE {?c a owl:Class} LIMIT 1000";

		ResultSet q = queryable.query(query);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			if(qs.getResource("c").isURIResource()){
				classes.add(new NamedClass(qs.getResource("c").getURI()));
			}

		}
		//remove trivial classes
		classes.remove(new NamedClass(OWL.Nothing.toStringID()));
		classes.remove(new NamedClass(OWL.Thing.toStringID()));
		return classes;
	}

	public Set<ObjectProperty> getAllObjectProperties() {
		Set<ObjectProperty> properties = new TreeSet<ObjectProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:ObjectProperty}";
		ResultSet q = queryable.query(query);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new ObjectProperty(qs.getResource("p").getURI()));
		}
		return properties;
	}

	public Set<DatatypeProperty> getAllDataProperties() {
		Set<DatatypeProperty> properties = new TreeSet<DatatypeProperty>();
		String query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> SELECT ?p WHERE {?p a owl:DatatypeProperty}";
		ResultSet q = queryable.query(query);
		while (q.hasNext()) {
			QuerySolution qs = q.next();
			properties.add(new DatatypeProperty(qs.getResource("p").getURI()));
		}
		return properties;
	}

	/**********************************************************************************************************************************************/

	private void serialize(){
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(file)));
			List<Map<String, Integer>> mapList = new ArrayList<Map<String,Integer>>();
			mapList.add(class2Popularity);
			mapList.add(property2Popularity);
			mapList.add(resource2Popularity);
			oos.writeObject(mapList);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(oos != null){
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private boolean deserialize(){
		File mapFile = new File(file);
		if(mapFile.exists()){
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(new FileInputStream(new File(file)));
				List<Map<String, Integer>> mapList = (List<Map<String, Integer>>) ois.readObject();
				class2Popularity = mapList.get(0);
				property2Popularity = mapList.get(1);
				resource2Popularity = mapList.get(2);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(ois != null){
					try {
						ois.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			System.out.println("Loaded popularity map.");
			return true;
		}
		return false;
	}

	private int loadPopularity(String uri, EntityType entityType){
		String query;
		if(entityType == EntityType.CLASS){
			query = String.format("SELECT COUNT(?s) WHERE {?s a <%s>}", uri);
		} else if(entityType == EntityType.PROPERTY){
			query = String.format("SELECT COUNT(*) WHERE {?s <%s> ?o}", uri);
		} else {
			query = String.format("SELECT COUNT(*) WHERE {?s ?p <%s>}", uri);
		}
		int pop = 0;
		ResultSet rs = queryable.query(query);
		QuerySolution qs;
		String projectionVar;
		while(rs.hasNext()){
			qs = rs.next();
			projectionVar = qs.varNames().next();
			pop = qs.get(projectionVar).asLiteral().getInt();
		}
		return pop;
	}

	public int getPopularity(String uri, EntityType entityType){
		Integer popularity;
		if(entityType == EntityType.CLASS){
			popularity = class2Popularity.get(uri);
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				class2Popularity.put(uri, popularity);
			}
		} else if(entityType == EntityType.PROPERTY){
			popularity = property2Popularity.get(uri);
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				property2Popularity.put(uri, popularity);
			}
		} else {
			popularity = resource2Popularity.get(uri);
			if(popularity == null){
				popularity = loadPopularity(uri, entityType);
				resource2Popularity.put(uri, popularity);
			}
		}
		return popularity;
	}

	public Integer getPopularity(String uri){
		Integer popularity  = class2Popularity.get(uri);
		if(popularity == null){
			popularity = property2Popularity.get(uri);
		}
		if(popularity == null){
			popularity = resource2Popularity.get(uri);
		}
		return popularity;
	}

	public static void main(String[] args) {
		PopularityMap map = new PopularityMap("dbpedia_popularity.map",new SparqlQueriable(SparqlEndpoint.getEndpointDBpediaLiveAKSW(), "cache"));
		System.out.println(map.getPopularity("http://dbpedia.org/ontology/Book"));
	}

}
