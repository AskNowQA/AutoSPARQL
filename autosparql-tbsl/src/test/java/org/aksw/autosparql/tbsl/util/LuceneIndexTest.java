package org.aksw.autosparql.tbsl.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class LuceneIndexTest
{

	@Test public void testGetData()
	{
		LuceneIndex index = new LuceneIndex("/home/konrad/projekte/java-maven/tbsl/oxford_index");
		System.out.println(index.getData("houses in headington", 10, 0));
	}

}
