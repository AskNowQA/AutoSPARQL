package org.aksw.autosparql.commons.nlp.wordnet;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.junit.Test;

public class WordNetUnpackerTest
{
	@Test public void testGetUnpackedWordNetDir() throws IOException
	{
		assertTrue(WordNetUnpacker.getUnpackedWordNetDir().toString().endsWith("wordnet/dict"));
	}

	/*@Test*/ public void testCopyResourcesToDirectory() throws IOException
	{
		File dir = new File("/tmp/test");
		dir.mkdir();
		WordNetUnpacker.copyResourcesToDirectory
		(new JarFile("/home/konrad/projekte/java-maven/AutoSPARQL/commons/target/commons-1.0-SNAPSHOT.jar"), "models/en/wordnet/dict", dir.getPath());
	}
}
