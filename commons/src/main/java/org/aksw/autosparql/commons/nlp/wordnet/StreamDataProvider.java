//package org.aksw.autosparql.commons.nlp.wordnet;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//import edu.mit.jwi.data.BinarySearchWordnetFile;
//import edu.mit.jwi.data.ContentType;
//import edu.mit.jwi.data.DataType;
//import edu.mit.jwi.data.DirectAccessWordnetFile;
//import edu.mit.jwi.data.IContentType;
//import edu.mit.jwi.data.IDataProvider;
//import edu.mit.jwi.data.IDataSource;
//import edu.mit.jwi.data.IDataType;
//import edu.mit.jwi.data.ILoadPolicy;
//import edu.mit.jwi.data.ILoadable;
//import edu.mit.jwi.data.ILoadableDataSource;
//import edu.mit.jwi.data.parse.ILineParser;
//import edu.mit.jwi.item.ISynset;
//import edu.mit.jwi.item.IVersion;
//import edu.mit.jwi.item.POS;
//import edu.mit.jwi.item.Synset;
//
///**Rewrite of the FileProvider that uses streams instead of files so 
// * that wordnet can be accessed even from within jarfiles which is mandatory in web applications.
// * @author Konrad Höffner
// * @version 0.1 alpha
// */
//public class StreamDataProvider implements IDataProvider, ILoadable, ILoadPolicy {
//	
//	// final instance fields
//	private final Lock lifecycleLock = new ReentrantLock();
//	private final Lock loadingLock = new ReentrantLock();
//	private final Set<IContentType<?>> types;
//
//	// instance fields
//	final private Class clazz;
//	final private String path;	 
//	
//	private URL url = null;	
//	private Map<IContentType<?>, ILoadableDataSource<?>> fileMap = null;
//	private int loadPolicy = NO_LOAD;
//	private transient JWIBackgroundLoader loader = null;	
//	
//	public IVersion getVersion() {return IVersion.NO_VERSION;}
//		
//	   /** Copied from http://www.uofr.net/~greg/java/get-resource-listing.html
//	   * List directory contents for a resource folder. Not recursive.
//	   * This is basically a brute-force implementation.
//	   * Works for regular files and also JARs.
//	   * 
//	   * @author Greg Briggs
//	   * @param clazz Any java class that lives in the same place as the resources you want.
//	   * @param path Should end with "/", but not start with one.
//	   * @return Just the name of each member item, not the full paths.
//	   * @throws URISyntaxException 
//	   * @throws IOException 
//	   */
//	  List<String> getResourceListing(Class clazz, String path) throws IOException
//	  {
//	      URL dirURL = clazz.getClassLoader().getResource(path);
//	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
//	        /* A file path: easy enough */
//	        try {return Arrays.asList(new File(dirURL.toURI()).list());}
//			catch (URISyntaxException e) {throw new IOException(e);}
//	      } 
//
//	      if (dirURL == null) {
//	        /* 
//	         * In case of a jar file, we can't actually find a directory.
//	         * Have to assume the same jar as clazz.
//	         */
//	        String me = clazz.getName().replace(".", "/")+".class";
//	        dirURL = clazz.getClassLoader().getResource(me);
//	      }
//	      
//	      if (dirURL.getProtocol().equals("jar")) {
//	        /* A JAR path */
//	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
//	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
//	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
//	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
//	        while(entries.hasMoreElements()) {
//	          String name = entries.nextElement().getName();
//	          if (name.startsWith(path)) { //filter according to the path
//	            String entry = name.substring(path.length());
//	            int checkSubdir = entry.indexOf("/");
//	            if (checkSubdir >= 0) {
//	              // if it is a subdirectory, we just return the directory name
//	              entry = entry.substring(0, checkSubdir);
//	            }
//	            result.add(entry);
//	          }
//	        }
//	        return new ArrayList<String>(result);
//	      } 
//	        
//	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
//	  }
//	  
//	/**
//	 * Constructs the stream data provider pointing to the resource indicated by the
//	 * path.  This file provider has an initial {@link ILoadPolicy#NO_LOAD} load policy.
//	 * 
//	 * @param url
//	 *            A file URL in UTF-8 decodable format, may not be
//	 *            <code>null</code>
//	 * @throws NullPointerException
//	 *             if the specified URL is <code>null</code>
//	 * @since JWI 1.0
//	 */
//	public StreamDataProvider(Class clazz, String path)
//	{
//		this(clazz,path, NO_LOAD);
//	}
//
//	/**
//	 * Constructs the stream data provider pointing to the resource indicated by the
//	 * path, with the specified load policy.
//	 * @param loadPolicy
//	 *            the load policy for this provider; this provider supports the
//	 *            three values defined in <code>ILoadPolicy</code>.
//	 * @throws NullPointerException
//	 *             if the specified URL is <code>null</code>
//	 */
//	public StreamDataProvider(Class clazz, String path, int loadPolicy)
//	{
//		this(clazz, path, loadPolicy, ContentType.values());
//	}
//
//	/**
//	 * Constructs the stream data provider pointing to the resource indicated by the
//	 * path, with the specified load policy, looking for the specified content
//	 * type.s
//	 * 
//	 * @param url
//	 *            A file URL in UTF-8 decodable format, may not be
//	 *            <code>null</code>
//	 * @param loadPolicy
//	 *            the load policy for this provider; this provider supports the
//	 *            three values defined in <code>ILoadPolicy</code>.
//	 * @param types
//	 *            the content types this provider will look for when it loads
//	 *            its data; may not be <code>null</code> or empty
//	 * @throws NullPointerException
//	 *             if the url or content type collection is <code>null</code>
//	 * @throws IllegalArgumentException
//	 *             if the set of types is empty
//	 * @since JWI 2.2.0
//	 */
//	public StreamDataProvider(Class clazz, String path, int loadPolicy, Collection<? extends IContentType<?>> types)
//	{
//		if(clazz == null) throw new NullPointerException();
//		if(path == null) throw new NullPointerException();
//		
//		if(types.isEmpty()) 
//			throw new IllegalArgumentException();
//		this.clazz = clazz;
//		this.path = path;
//		this.loadPolicy = loadPolicy;
//		this.types = Collections.unmodifiableSet(new HashSet<IContentType<?>>(types));
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see edu.mit.jwi.data.IDataProvider#getSource()
//	 */
//	public URL getSource() {
//		return url;
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.ILoadPolicy#getLoadPolicy()
//	 */
//	public int getLoadPolicy() {
//		return loadPolicy;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see edu.mit.jwi.data.IDataProvider#setSource(java.net.URL)
//	 */
//	public void setSource(URL url) {
//		if(isOpen()) 
//			throw new IllegalStateException("provider currently open");
//		if(url == null) 
//			throw new NullPointerException();
//		this.url = url;
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.ILoadPolicy#setLoadPolicy(int)
//	 */
//	public void setLoadPolicy(int policy) {
//		try{
//			loadingLock.lock();
//			this.loadPolicy = policy; 	
//		} finally {
//			loadingLock.unlock();
//		}
//	}
//
//	/**
//	 * Determines a version from the set of data sources, if possible, otherwise
//	 * returns {@link IVersion#NO_VERSION}
//	 * 
//	 * @param srcs
//	 *            the data sources to be used to determine the verison
//	 * @return the single version that describes these data sources, or
//	 *        {@link IVersion#NO_VERSION} if there is none
//	 * @since JWI 2.1.0
//	 */
//	protected IVersion determineVersion(Collection<? extends IDataSource<?>> srcs){
//		IVersion ver = IVersion.NO_VERSION;
//		for(IDataSource<?> dataSrc : srcs){
//			
//			// if no version to set, ignore
//			if(dataSrc.getVersion() == null)
//				continue;
//	
//			// init version
//			if(ver == IVersion.NO_VERSION){
//				ver = dataSrc.getVersion();
//				continue;
//			} 
//			
//			// if version different from current
//			if(!ver.equals(dataSrc.getVersion())) 
//				return IVersion.NO_VERSION;
//		}
//		
//		return ver;
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.IHasLifecycle#open()
//	 */
//	public boolean open() throws IOException {
//		try {
//			lifecycleLock.lock();
//			loadingLock.lock();
//			
//			int policy = getLoadPolicy();
//			
//			// make sure directory exists and contains something
//			
//			List<String>resources = getResourceListing(clazz, path);
//			if(resources.isEmpty())
//				throw new IOException("Dictionary does not contain any resources: " + path+" when loaded from classloader of class "+clazz);
//			
//			// make the source map
//			Map<IContentType<?>, ILoadableDataSource<?>> hiddenMap = createSourceMap(resources, policy);
//			if(hiddenMap.isEmpty()) 
//				return false;
//			
//			// determine if it's already unmodifiable, wrap if not
//			Map<?,?> map = Collections.unmodifiableMap(Collections.emptyMap());
//			if(hiddenMap.getClass() != map.getClass())
//				hiddenMap = Collections.unmodifiableMap(hiddenMap);
//			this.fileMap = hiddenMap;
//			
//			// do load
//			try {
//				switch(loadPolicy){
//				case BACKGROUND_LOAD:
//					load(false);
//					break;
//				case IMMEDIATE_LOAD:
//					load(true);
//					break;
//				default:
//					// do nothing
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			return true;
//		} finally {
//			lifecycleLock.unlock();
//			loadingLock.unlock();
//		}
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.ILoadable#load()
//	 */
//	public void load() {
//		try {
//			load(false);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.ILoadable#load(boolean)
//	 */
//	public void load(boolean block) throws InterruptedException {
//		try{
//			loadingLock.lock();
//			checkOpen();
//			if(isLoaded()) 
//				return;
//			if(loader != null)
//				return;
//			loader = new JWIBackgroundLoader();
//			loader.start();
//			if(block) 
//				loader.join();
//		} finally {
//			loadingLock.lock();
//		}
//
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.ILoadable#isLoaded()
//	 */
//	public boolean isLoaded() {
//		if(!isOpen()) 
//			throw new IllegalStateException("provider not open");
//		try {
//			loadingLock.lock();
//			for(ILoadableDataSource<?> source : fileMap.values())
//				if(!source.isLoaded()) 
//					return false;
//			return true;
//		} finally{
//			loadingLock.unlock();
//		}
//	}
//
//	/* copied and changed from DataType.find() */
// 	public static String find(IDataType<?> type, POS pos, Collection<String> resources){
//
//		Set<String> typePatterns = type.getResourceNameHints();
//		Set<String> posPatterns = (pos == null) ? Collections.<String>emptySet() : pos.getResourceNameHints();
//		
//		String name;
//		for (String resource : resources )
//		{
//			name = resource.toLowerCase();
//			if(DataType.containsOneOf(name, typePatterns) && DataType.containsOneOf(name, posPatterns)) 
//				return resource;
//		}		
//		return null;
//	}
//	
//	/**
//	 * Creates the map that contains the content types mapped to the data
//	 * sources. The method should return a non-null result, but it may be empty
//	 * if no data sources can be created. Subclasses may override this method.
//	 * 
//	 * @param resources
//	 *            the files from which the data sources should be created, may
//	 *            not be <code>null</code>
//	 * @param policy
//	 *            the load policy of the provider
//	 * @return a map, possibly empty, but not <code>null</code>, of content
//	 *         types mapped to data sources
//	 * @throws NullPointerException
//	 *             if the file list is <code>null</code>
//	 * @throws IOException
//	 *             if there is a problem creating the data source
//	 * @since JWI 2.2.0
//	 */
//	protected Map<IContentType<?>, ILoadableDataSource<?>> createSourceMap(List<String> resources, int policy) throws IOException {
//		Map<IContentType<?>, ILoadableDataSource<?>> result = new HashMap<IContentType<?>, ILoadableDataSource<?>>();
//		String resource;
//		for (IContentType<?> type : types) {
//			resource = find(type.getDataType(), type.getPOS(), resources);
//			if(resource == null) continue;
//			resources.remove(resource);
//			result.put(type, createDataSource(resource, type, policy));
//		}
//		return result;
//	}
//
//	/**
//	 * Creates the actual data source implementations.
//	 * 
//	 * @param <T>
//	 *            the content type of the data source
//	 * @param file
//	 *            the file from which the data source should be created, may not
//	 *            be <code>null</code>
//	 * @param type
//	 *            the content type of the data source
//	 * @param policy
//	 *            the load policy to follow when creating the data source
//	 * @return the created data source
//	 * @throws NullPointerException
//	 *             if any argument is <code>null</code>
//	 * @throws IOException
//	 *             if there is an IO problem when creating the data source
//	 * @since JWI 2.2.0
//	 */
//	protected <T> ILoadableDataSource<T> createDataSource(String resource, IContentType<T> type, int policy) throws IOException {
//		
//		ILoadableDataSource<T> src;
//		if(type.getDataType() == DataType.DATA){
//			
//			src = createDirectAccess(resource, type);
//			src.open();
//			if(policy == IMMEDIATE_LOAD) {
//				try {
//					src.load(true);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			// check to see if direct access works with the file
//			// often people will extract the files incorrectly on windows machines
//			// and the binary files will be corrupted with extra CRs
//			
//			// get first line
//			Iterator<String> itr = src.iterator();
//			String firstLine = itr.next();
//			if(firstLine == null) return src;
//			
//			// extract key
//			ILineParser<T> parser = type.getDataType().getParser();
//			ISynset s = (ISynset)parser.parseLine(firstLine);
//			String key = Synset.zeroFillOffset(s.getOffset());
//			
//			// try to find line by direct access
//			String soughtLine = src.getLine(key);
//			if(soughtLine != null) return src;
//			
//			System.err.println(System.currentTimeMillis() + " - Error on direct access in " + type.getPOS().toString() + " data file: check CR/LF endings");
//		}
//		
//		src = createBinarySearch(file, type);
//		src.open();
//		if(policy == IMMEDIATE_LOAD){
//			try {
//				src.load(true);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		return src;
//	}
//
//	/**
//	 * Creates a direct access data source for the specified type, using the
//	 * specified file.
//	 * 
//	 * @param <T>
//	 *            the parameter of the content type
//	 * @param file
//	 *            the file on which the data source is based; may not be
//	 *            <code>null</code>
//	 * @param type
//	 *            the data type for the data source; may not be
//	 *            <code>null</code>
//	 * @return the data source
//	 * @throws NullPointerException
//	 *             if either argument is <code>null</code>
//	 * @throws IOException
//	 *             if there is an IO problem when creating the data source
//	 *             object
//	 * @since JWI 2.2.0
//	 */
//	protected <T> ILoadableDataSource<T> createDirectAccess(String resource, IContentType<T> type) throws IOException
//	{		
//		return new WordNetResource<T>(resource, type);
//	}
//
//	/**
//	 * Creates a binary search data source for the specified type, using the
//	 * specified file.
//	 * 
//	 * @param <T>
//	 *            the parameter of the content type
//	 * @param file
//	 *            the file on which the data source is based; may not be
//	 *            <code>null</code>
//	 * @param type
//	 *            the data type for the data source; may not be
//	 *            <code>null</code>
//	 * @return the data source
//	 * @throws NullPointerException
//	 *             if either argument is <code>null</code>
//	 * @throws IOException
//	 *             if there is an IO problem when creating the data source
//	 *             object
//	 * @since JWI 2.2.0
//	 */
//	protected <T> ILoadableDataSource<T> createBinarySearch(File file, IContentType<T> type) throws IOException {
//		return new BinarySearchWordnetFile<T>(file, type);
//	}
//	
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.IHasLifecycle#isOpen()
//	 */
//	public boolean isOpen() {
//		try {
//			lifecycleLock.lock();
//			return fileMap != null;
//		} finally {
//			lifecycleLock.unlock();
//		}
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.IClosable#close()
//	 */
//	public void close() {
//		try {
//			lifecycleLock.lock();
//			if(!isOpen()) 
//				return;
//			if(loader != null) 
//				loader.cancel();
//			for(IDataSource<?> source : fileMap.values()) 
//				source.close();
//			fileMap = null;
//		} finally {
//			lifecycleLock.unlock();
//		}
//	}
//
//	/**
//	 * Convenience method that throws an exception if the provider is closed.
//	 * 
//	 * @throws ObjectClosedException
//	 *             if the provider is closed
//	 * @since JWI 1.1
//	 */
//	protected void checkOpen() {
//		if(!isOpen()) 
//			throw new ObjectClosedException();
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.IDataProvider#getSource(edu.mit.jwi.data.IContentType)
//	 */
//	// no way to safely cast; must rely on registerSource method to assure compliance
//	@SuppressWarnings("unchecked") 
//	public <T> ILoadableDataSource<T> getSource(IContentType<T> type) {
//		checkOpen();
//		return (ILoadableDataSource<T>)fileMap.get(type);
//	}
//
//	/* 
//	 * (non-Javadoc) 
//	 *
//	 * @see edu.mit.jwi.data.IDataProvider#getTypes()
//	 */
//	public Set<? extends IContentType<?>> getTypes() {
//		return types;
//	}
//
//	/**
//	 * A thread class which tries to load each data source in this provider.
//	 * 
//	 * @author Mark A. Finlayson
//	 * @version 2.3.1
//	 * @since JWI 2.2.0
//	 */
//	protected class JWIBackgroundLoader extends Thread {
//		
//		// cancel flag
//		private transient boolean cancel = false;
//		
//		/** 
//		 * Constructs a new background loader that operates
//		 * on the internal data structures of this provider.
//		 *
//		 * @since JWI 2.2.0
//		 */
//		public JWIBackgroundLoader(){
//			setName(JWIBackgroundLoader.class.getSimpleName());
//			setDaemon(true);
//		}
//
//		/* 
//		 * (non-Javadoc) 
//		 *
//		 * @see java.lang.Thread#run()
//		 */
//		@Override
//		public void run() {
//			try {
//				for(ILoadableDataSource<?> source : fileMap.values()){
//					if(!cancel && !source.isLoaded()){
//						try {
//							source.load(true);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			} finally {
//				loader = null;
//			}
//		}
//
//		/** 
//		 * Sets the cancel flag for this loader. 
//		 *
//		 * @since JWI 2.2.0
//		 */
//		public void cancel() {
//			cancel = true;
//			try {
//				join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//	}
//
//	/**
//	 * Transforms a URL into a File. The URL must use the 'file' protocol and
//	 * must be in a UTF-8 compatible format as specified in
//	 * {@link java.net.URLDecoder}.
//	 * 
//	 * @return a file pointing to the same place as the url
//	 * @throws NullPointerException
//	 *             if the url is <code>null</code>
//	 * @throws IllegalArgumentException
//	 *             if the url does not use the 'file' protocol
//	 * @since JWI 1.0
//	 */
//	public static File toFile(URL url) throws IOException {
//		if(!url.getProtocol().equals("file")) 
//			throw new IllegalArgumentException("URL source must use 'file' protocol");
//		try {
//			return new File(URLDecoder.decode(url.getPath(), "UTF-8"));
//		} catch(UnsupportedEncodingException e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	/**
//	 * Transforms a file into a URL.
//	 * 
//	 * @param file
//	 *            the file to be transformed
//	 * @return a URL representing the file
//	 * @throws NullPointerException
//	 *             if the specified file is <code>null</code>
//	 * @since JWI 2.2.0
//	 */
//	public static URL toURL(File file) {
//		if(file == null)
//			throw new NullPointerException();
//		try{
//			URI uri = new URI("file", "//", file.toURL().getPath() , null);
//			return new URL("file", null, uri.getRawPath());
//		} catch (IOException e) {
//			e.printStackTrace();
//			return null;
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//}