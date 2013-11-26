package org.aksw.autosparql.commons.index.dbpedia;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

public class DBpediaYAGOClassesSolrIndexCreator {
	
	private SolrInputField uriField = new SolrInputField("uri");
	private SolrInputField labelField = new SolrInputField("label");
	private SolrInputField commentField = new SolrInputField("comment");
	
	private SolrInputDocument doc;
	private HttpSolrServer solr;
	
	private Set<SolrInputDocument> docs = new HashSet<SolrInputDocument>();
	
	private static final int COMMIT_SIZE = 1000;//number of documents in a batch
	
	public DBpediaYAGOClassesSolrIndexCreator(String solrIndexServerURL, String coreName){
		solr = new HttpSolrServer(solrIndexServerURL + "/" + coreName);
		solr.setRequestWriter(new BinaryRequestWriter());
		
        initDocument();
	}
	
	
	public void createIndex(String versionNumber, String languageTag){
		InputStream is = loadDBpediaCategoriesFile(versionNumber, languageTag);
		createIndex(is);
	}
	
	/**
	 * The DBpedia categories only have labels and no comments, i.e. we do not have to sort the file and can just
	 * iterate over the categories.
	 * @param is
	 */
	public void createIndex(InputStream is){
		RDFFormat format = RDFFormat.NTRIPLES;
		RDFParser parser = Rio.createParser(format);
		parser.setRDFHandler(new RDFHandler() {
			int cnt = 1;
			String uri = "";
			String label = "";
			String comment = "";
			@Override
			public void startRDF() throws RDFHandlerException {}
			
			@Override
			public void handleStatement(org.openrdf.model.Statement stmt) throws RDFHandlerException {
				uri = stmt.getSubject().stringValue();
				uri = uri.replace("http://dbpedia.org/resource/Category:", "http://dbpedia.org/class/yago/");
				label = stmt.getObject().stringValue();
				if(uri.contains("Russian_cos")){
					System.out.println(label);
					System.out.println(uri);
				}
//				addDocument(uri, label, comment);
//				cnt++;
//				if(cnt % COMMIT_SIZE == 0){
//					write2Index();
//				}
			}
			
			@Override
			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {}
			@Override
			public void handleComment(String arg0) throws RDFHandlerException {}
			@Override
			public void endRDF() throws RDFHandlerException {}
		});
		try {
			parser.parse(new BufferedInputStream(is), "http://dbpedia.org");
			write2Index();
			solr.commit();
			solr.optimize();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} 
	}
	
	private InputStream loadDBpediaCategoriesFile(String version, String languageTag) {
		//http://downloads.dbpedia.org/3.8/en/category_labels_en.nt.bz2
		try {
			URL dbpediaURL = new URL(
					"http://downloads.dbpedia.org/" + version + "/" + languageTag + "/category_labels_" + languageTag + ".nt.bz2");
			InputStream is = dbpediaURL.openStream();
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			return is;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initDocument(){
		doc = new SolrInputDocument();
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
	}
	
	private void addDocument(String uri, String label, String comment){
		doc = new SolrInputDocument();
		uriField = new SolrInputField("uri");
		labelField = new SolrInputField("label");
		commentField = new SolrInputField("comment");
		doc.put("uri", uriField);
		doc.put("label", labelField);
		doc.put("comment", commentField);
		uriField.setValue(uri, 1.0f);
		labelField.setValue(label, 1.0f);
		commentField.setValue(comment, 1.0f);
		
		docs.add(doc);
	}
	
	private void write2Index(){
		try {
			solr.add(docs);
			docs.clear();
		}  catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 4){
			System.out.println("Usage: DBpediaYAGOClassesSolrIndexCreator <SOLR-Server-URL> <SOLR-Core-Name> <DBpedia-Version-Number> <Language-Tag> ");
			System.exit(0);
		}
		
		String solrServerURL = args[0];
		String solrCoreName = args[1];
		String versionNumber = args[2];
		String languageTag = args[3];
		
		new DBpediaYAGOClassesSolrIndexCreator(solrServerURL, solrCoreName).createIndex(versionNumber, languageTag);
	}

}
