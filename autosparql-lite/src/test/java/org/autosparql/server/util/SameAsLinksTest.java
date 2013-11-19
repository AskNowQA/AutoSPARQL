package org.autosparql.server.util;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.aksw.autosparql.server.util.SameAsLinks;
import org.junit.Test;

public class SameAsLinksTest {

	/** this test could be a bit unstable - if this test fails check
	if there are multiple sameas links for leipzig (because only one gets chosen) or new urls for showing were added to the class SameAsLinks. */
	@Test
	public void testGetSameAsLinksForShowing()
	{		
		List<String> links = SameAsLinks.getSameAsLinksForShowing("http://dbpedia.org/resource/Leipzig");
		assertTrue(new HashSet<String>(links).equals(new HashSet<String>(Arrays.asList(new String[]
				{"http://rdf.freebase.com/ns/guid.9202a8c04000641f80000000000245a4",
				"http://sws.geonames.org/2879139/",
				"http://en.wikipedia.org/wiki/Leipzig"}))));
	}
}