package org.autosparql.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionUtils
{
	/** creates a list out of the toString()-values of a collection*/
	public static List<String> toString(Collection<?> c)
	{
		List<String> list = new ArrayList<>();
		for(Object o: c) {list.add(o.toString());}
		return list;
	}
}