package org.aksw.autosparql.tbsl.gui.vaadin.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndex implements FallbackIndex{
	
	private IndexSearcher searcher;
	
	public LuceneIndex(String directoryPath) {
		 try {
			Directory index = new RAMDirectory(NIOFSDirectory.open(new File(directoryPath)),IOContext.READ);
//			 IndexReader reader = IndexReader.open(index);
			IndexReader reader = DirectoryReader.open(index);
			 searcher = new IndexSearcher(reader);
		} catch (CorruptIndexException e) {
			 e.printStackTrace();
	            throw new RuntimeException("Could not create index", e);
		} catch (IOException e) {
			 e.printStackTrace();
	            throw new RuntimeException("Could not create index", e);
		}
	}
	
	public List<BasicResultItem> getData(String queryString, int limit, int offset) {
		List<BasicResultItem> items = new ArrayList<BasicResultItem>();
        try {
			Query q = new QueryParser(Version.LUCENE_46, "description", new StandardAnalyzer(Version.LUCENE_36)).parse(queryString);
			TopScoreDocCollector collector = TopScoreDocCollector.create(limit, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			for(int i=0;i<hits.length;++i) {
			    int docId = hits[i].doc;
			    Document d = searcher.doc(docId);
			    String uri = d.get("uri");
			    String label = d.get("label");
			    String description = d.get("description");
			    String imageURL = d.get("imageURL");
			    Map<String, Object> data = new HashMap<String, Object>();
			    if(d.get("address") != null){
			    	data.put("street", d.get("address"));
			    }
			    if(d.get("locality") != null){
			    	data.put("locality", d.get("locality"));
			    }
			    if(d.get("price") != null){
			    	 data.put("price", Double.valueOf(d.get("price")));
			    }
			    if(d.get("bedrooms") != null){
			    	 data.put("bedrooms", Integer.valueOf(d.get("bedrooms")));
			    }
			    if(d.get("bathrooms") != null){
			    	 data.put("bathrooms", Integer.valueOf(d.get("bathrooms")));
			    }
			    if(d.get("receptions") != null){
			    	 data.put("receptions", Integer.valueOf(d.get("receptions")));
			    }
			    
			    BasicResultItem item = new BasicResultItem(uri, label, description, imageURL, data);
			    items.add(item);
			}
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
        return items;
	}
	
}