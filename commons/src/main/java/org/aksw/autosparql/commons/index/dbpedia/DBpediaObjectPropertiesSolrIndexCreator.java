package org.aksw.autosparql.commons.index.dbpedia;

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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class DBpediaObjectPropertiesSolrIndexCreator {

	private SolrInputField uriField = new SolrInputField("uri");
	private SolrInputField labelField = new SolrInputField("label");
	private SolrInputField commentField = new SolrInputField("comment");

	private SolrInputDocument doc;
	private HttpSolrServer solr;

	private Set<SolrInputDocument> docs = new HashSet<SolrInputDocument>();

	private static final int COMMIT_SIZE = 1000;//number of documents in a batch

	public DBpediaObjectPropertiesSolrIndexCreator(String solrIndexServerURL, String coreName){
		solr = new HttpSolrServer(solrIndexServerURL + "/" + coreName);
		solr.setRequestWriter(new BinaryRequestWriter());

        initDocument();
	}

	public void createIndex(String versionNumber, String languageTag){
		OWLOntology ontology = loadDBpediaOntology(versionNumber);
		createIndex(ontology, languageTag);
	}

	public void createIndex(OWLOntology ontology, String language){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLAnnotationProperty labelProperty = man.getOWLDataFactory().getRDFSLabel();
			OWLAnnotationProperty commentProperty = man.getOWLDataFactory().getRDFSComment();
			String uri = "";
			String label = "";
			String comment = "";
			int cnt = 1;
			for(OWLObjectProperty prop : ontology.getObjectPropertiesInSignature()){
				uri = prop.toStringID();
				label = "";
				comment = "";
				for(OWLAnnotation lab : prop.getAnnotations(ontology, labelProperty)){
					if(lab.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)lab.getValue();
						if(lit.hasLang(language)){
							label = lit.getLiteral();
						}
					}

				}
				for(OWLAnnotation com : prop.getAnnotations(ontology, commentProperty)){
					if(com.getValue() instanceof OWLLiteral){
						OWLLiteral lit = (OWLLiteral)com.getValue();
						if(lit.hasLang(language)){
							comment = lit.getLiteral();
						}
					}
				}
				addDocument(uri, label, comment);
				if(cnt % COMMIT_SIZE == 0){
					write2Index();
				}
			}
			write2Index();
			solr.commit();
			solr.optimize();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private OWLOntology loadDBpediaOntology(String version) {
		OWLOntology ontology = null;
		try {
			URL dbpediaURL = new URL("http://downloads.dbpedia.org/" + version + "/dbpedia_" + version + ".owl.bz2");
			InputStream is = dbpediaURL.openStream();
			is = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CompressorException e) {
			e.printStackTrace();
		}
		return ontology;
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
			System.out.println("Usage: DBpediaObjectPropertiesSolrIndexCreator <SOLR-Server-URL> <SOLR-Core-Name> <DBpedia-Version-Number> <Language-Tag> ");
			System.exit(0);
		}

		String solrServerURL = args[0];
		String solrCoreName = args[1];
		String versionNumber = args[2];
		String languageTag = args[3];

		new DBpediaObjectPropertiesSolrIndexCreator(solrServerURL, solrCoreName).createIndex(versionNumber, languageTag);
	}

}
