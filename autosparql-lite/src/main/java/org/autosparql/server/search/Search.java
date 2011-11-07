package org.autosparql.server.search;

import java.util.List;
import java.util.SortedSet;

import org.autosparql.shared.Example;

public interface Search
{
	List<String> getResources(String query);	
	List<String> getResources(String query, int limit);
	List<String> getResources(String query, int limit, int offset);	
	SortedSet<Example> getExamples(String query);
	SortedSet<Example> getExamples(String query, int limit);
	SortedSet<Example> getExamples(String query, int limit, int offset);
}