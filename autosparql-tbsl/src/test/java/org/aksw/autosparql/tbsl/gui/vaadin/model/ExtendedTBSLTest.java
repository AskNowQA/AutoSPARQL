package org.aksw.autosparql.tbsl.gui.vaadin.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class ExtendedTBSLTest
{

	@Test public void testOxford()
	{
		assertTrue(ExtendedTBSL.OXFORD.getExampleQuestions().contains("houses in Summertown"));
	}

}