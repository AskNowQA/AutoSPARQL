package org.aksw.autosparql.commons.index;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LemmatizedIndexTest
{

	@Test public void testLemmatize()
	{
		String[][] pluralToSingular = {{"houses","house"},{"indices","index"},{"cities","city"},{"bacteria","bacterium"},{"geese","goose"}};
		for(String[] pluralSingular : pluralToSingular)
		{
			assertEquals(pluralSingular[1],LemmatizedIndex.lemmatize(pluralSingular[0]));
		}
	}

}