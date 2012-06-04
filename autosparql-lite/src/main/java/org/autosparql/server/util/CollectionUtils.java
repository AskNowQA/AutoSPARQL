package org.autosparql.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** "Extends" java.util.Collections. */
public final class CollectionUtils
{	
	/** creates a list out of the toString()-values of a collection*/
	public static List<String> toStringList(Collection<?> c)
	{
		List<String> list = new ArrayList<String>();
		for(Object o: c) {list.add(o.toString());}
		return list;
	}
	
	private CollectionUtils() {throw new AssertionError();}
}