package org.aksw.autosparql.server.properties;
//package org.autosparql.server.properties;
//
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
///** Determines which properties are shown for a set of instances initially and which are additionally available.
// *  @author Konrad Höffner */
//public class DefaultPropertyChooser
//{
//	Set<String> blacklist;
//	//Object Map<Object, Set<Object>> cache;
//	
//	List<String> getInitialProperties(Object instances)
//	{
//		// get common properties
//		LinkedHashSet<String> initialProperties = null; // common properties
//		initialProperties.removeAll(blacklist);
//		return new ArrayList<String>(initialProperties);
//	}
//
// //public 
//}