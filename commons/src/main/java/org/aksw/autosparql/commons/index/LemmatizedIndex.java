package org.aksw.autosparql.commons.index;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import org.aksw.autosparql.commons.nlp.lemma.StanfordLemmatizer;
import org.aksw.autosparql.commons.nlp.pling.PlingStemmer;
import org.aksw.rdfindex.Index;
import org.aksw.rdfindex.IndexItem;
import org.aksw.rdfindex.IndexResultSet;

/** Index decorator that uses tries lemmatizing on the input in case of failure to find.**/
// TODO somehow get sentence for context or even better tag in the index
// Options: set map from word to tag -> disadvantage: state based, bad for multithreading
public class LemmatizedIndex extends Index
{
	protected Index index;
	static StanfordLemmatizer lemmatizer = new StanfordLemmatizer();
	static final float LEMMATIZED_PENALITY = 0.8f;

	public LemmatizedIndex(Index index) {this.index=index;}

	public static String lemmatize(String s)
	{return lemmatizer.stem(s);}

	@Override public IndexResultSet getResourcesWithScores(String queryString, int limit)
	{
		IndexResultSet items = index.getResourcesWithScores(queryString, limit);
		if(items.size()<limit)
		{
			Set<String> uris = new HashSet<String>();
			for(IndexItem item: items) {uris.add(item.getUri());}
			SortedSet<IndexItem> newItems = index.getResourcesWithScores(PlingStemmer.stem(queryString), limit-items.size());
			for(IndexItem item:newItems)
			{
				float newScore = item.getScore()*LEMMATIZED_PENALITY;
				// todo: replace with new score if higher or somehow aggregate
				if(!uris.contains(item.getUri()))
				{
					uris.add(item.getUri());
					items.add(new IndexItem(item.getUri(),item.getLabel(),newScore));
				}
			}
		}
		return items;
	}

}