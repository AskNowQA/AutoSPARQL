package org.aksw.autosparql.commons.nlp.wordnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Allows WordNet to be run from within a jar file by unpacking it to a temporary directory.**/
public class WordNetUnpacker
{
	static final String ID = "178558556719"; // minimize the chance of interfering  with an existing directory	
	static final String jarDir = "models/en/wordnet/dict";

	/**If running from within a jar, unpack wordnet from the jar to a temp directory (if not already done) and return that.
	 * If not running from a jar, just return the existing wordnet directory.
	 * @see getUnpackedWordNetDir(Class)*/
	static File getUnpackedWordNetDir() throws IOException
	{return getUnpackedWordNetDir(WordNetUnpacker.class);}

	/**If running from within a jar, unpack wordnet from the jar to a temp directory (if not already done) and return that.
	 * If not running from a jar, just return the existing wordnet directory.
	 * @param clazz the class in whose classloader the wordnet resources are found.
	 * @see getUnpackedWordNetDir()**/

	static File getUnpackedWordNetDir(Class clazz) throws IOException
	{
		String codeSource = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println("getUnpackedWordNetDir: using code source "+codeSource);
		if(!codeSource.endsWith(".jar"))
		{
			System.out.println("not running from jar, no unpacking necessary");
			try{return new File(WordNetUnpacker.class.getClassLoader().getResource(jarDir).toURI());}
			catch (URISyntaxException e) {throw new IOException(e);}
		}
		try(JarFile jarFile = new JarFile(codeSource))
		{
			String tempDirString = System.getProperty("java.io.tmpdir");
			if(tempDirString==null) {throw new IOException("java.io.tmpdir not set");}
			File tempDir = new File(tempDirString);
			if(!tempDir.exists()) {throw new IOException("temporary directory does not exist");}
			if(!tempDir.isDirectory()) {throw new IOException("temporary directory is a file, not a directory ");}
			File wordNetDir = new File(tempDirString+'/'+"wordnet"+ID);
			wordNetDir.mkdir();
			System.out.println("unpacking jarfile "+jarFile.getName());
			copyResourcesToDirectory(jarFile, jarDir, wordNetDir.getAbsolutePath());
			return wordNetDir;
		}		
	}
	/** Copies a directory from a jar file to an external directory. Copied from <a href="http://stackoverflow.com/a/19859453/398963">Stack Overflow</a>. */
	public static void copyResourcesToDirectory(JarFile fromJar, String jarDir, String destDir) throws IOException
	{
		int copyCount = 0;
		for (Enumeration<JarEntry> entries = fromJar.entries(); entries.hasMoreElements();)
		{
			JarEntry entry = entries.nextElement();
			if(!entry.getName().contains("models")) continue;
			if (entry.getName().startsWith(jarDir) && !entry.isDirectory()) {
				copyCount++;
				File dest = new File(destDir + "/" + entry.getName().substring(jarDir.length() + 1));
				File parent = dest.getParentFile();
				if (parent != null) {
					parent.mkdirs();
				}

				FileOutputStream out = new FileOutputStream(dest);
				InputStream in = fromJar.getInputStream(entry);

				try {
					byte[] buffer = new byte[8 * 1024];

					int s = 0;
					while ((s = in.read(buffer)) > 0) {
						out.write(buffer, 0, s);
					}
				} catch (IOException e) {
					throw new IOException("Could not copy asset from jar file", e);
				} finally {
					try {
						in.close();
					} catch (IOException ignored) {}
					try {
						out.close();
					} catch (IOException ignored) {}
				}
			}
		}
		if(copyCount==0) System.out.println("Warning: No files copied!");
	}
}
