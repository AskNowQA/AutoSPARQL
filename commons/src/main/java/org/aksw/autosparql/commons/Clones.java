package org.aksw.autosparql.commons;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** @author Konrad Höffner
 * Helper methods for deep cloning of collections.  */
public class Clones
{
	static void fromArrayToCollection(Object[] a, Collection<?> c) {
	}
	
	static <T extends Cloneable> Set<T> deepCloneSet(Set<T> s)
	{
		Set<T> clone = new HashSet<T>();
		return null;
	}

}
