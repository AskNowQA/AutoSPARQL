package org.aksw.autosparql.tbsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;
import org.aksw.autosparql.algorithm.tbsl.util.RemoteKnowledgebase;
import org.aksw.autosparql.commons.nlp.pos.PartOfSpeechTagger;
import org.aksw.autosparql.commons.nlp.pos.StanfordPartOfSpeechTagger;
import org.aksw.autosparql.commons.nlp.wordnet.WordNet;
import org.aksw.autosparql.tbsl.model.ExtendedKnowledgebase;
import org.aksw.autosparql.tbsl.model.InfoTemplate;
import org.aksw.autosparql.tbsl.util.FallbackIndex;
import org.aksw.autosparql.tbsl.util.LuceneIndex;
import org.aksw.autosparql.tbsl.util.SolrIndex;
import org.aksw.autosparql.tbsl.widget.DBpediaInfoLabel;
import org.aksw.autosparql.tbsl.widget.OxfordInfoLabel;
import org.apache.log4j.Logger;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.VirtuosoClassesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.common.index.VirtuosoResourcesIndex;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vaadin.terminal.ThemeResource;

public class Manager {
	
	private static final Logger logger = Logger.getLogger(TBSLManager.class);
	
	private static Manager instance;
	
	private PartOfSpeechTagger posTagger;
	private WordNet wordNet;
	private List<ExtendedKnowledgebase> knowledgebases;
	
	private String wordnetDir;
	private String cacheDir;
	private String oxfordFallbackIndexDir;
	private String semMapURL;
	
	private Manager() {
	}
	
	public void init(){
		logger.info("Initializing global settings...");
		loadSettings();
		
		posTagger = new StanfordPartOfSpeechTagger();
		wordNet = new WordNet(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
		logger.info("...done.");
	}
	
	private void loadSettings(){
		InputStream is;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("settings.ini");
			Ini ini = new Ini(is);
			//base section
			Section baseSection = ini.get("base");
			cacheDir = baseSection.get("cacheDir", String.class);
			wordnetDir = baseSection.get("wordnetDir", String.class);
			oxfordFallbackIndexDir = baseSection.get("oxfordFallbackIndexDir", String.class);
			semMapURL = baseSection.get("SemMapURL", String.class);
		
		} catch (InvalidFileFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
	}
	
	private ExtendedKnowledgebase createDBpediaKnowledgebase(ExtractionDBCache cache){
		try {
			SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"), Collections.singletonList("http://dbpedia.org"), Collections.<String>emptyList());
			
			SOLRIndex resourcesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
			resourcesIndex.setPrimarySearchField("label");
//			resourcesIndex.setSortField("pagerank");
			Index classesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_classes");
			Index propertiesIndex = new SOLRIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_properties");
			
			MappingBasedIndex mappingIndex= new MappingBasedIndex(
					this.getClass().getClassLoader().getResource("dbpedia_class_mappings.txt").getPath(), 
					this.getClass().getClassLoader().getResource("dbpedia_resource_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("dbpedia_dataproperty_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("dbpedia_objectproperty_mappings.txt").getPath()
					);
			
			Knowledgebase kb = new RemoteKnowledgebase(endpoint, "DBpedia Live", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
			String infoTemplateHtml = "<div><h3><b>label</b></h3></div>" +
        	 		"<div style='float: right; height: 100px; width: 200px'>" +
        	 		"<div style='height: 100%;'><img style='height: 100%;' src=\"imageURL\"/></div>" +
        	 		"</div>" +
        	 		"<div>description</div>";
			Map<String, String> propertiesMap = new HashMap<String, String>();
			propertiesMap.put("label", "http://www.w3.org/2000/01/rdf-schema#label");
			propertiesMap.put("imageURL", "http://www.w3.org/2000/01/rdf-schema#comment");
			propertiesMap.put("description", "http://xmlns.com/foaf/0.1/depiction");
			InfoTemplate infoTemplate = new InfoTemplate(infoTemplateHtml, null);
			
			List<String> exampleQuestions = loadQuestions(this.getClass().getClassLoader().getResourceAsStream("dbpedia_example_questions.txt"));
			
			ExtendedKnowledgebase ekb = new ExtendedKnowledgebase(
					kb, 
					"http://www.w3.org/2000/01/rdf-schema#label", 
					"http://www.w3.org/2000/01/rdf-schema#comment",
					"http://xmlns.com/foaf/0.1/depiction", null, null, DBpediaInfoLabel.class, "x0", true, exampleQuestions);
			ekb.setLabelPropertyLanguage("en");
			ekb.setPropertyNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));
			
			Set<String> propertyBlackList = loadPropertyBlackList("dbpedia_property_blacklist.txt");
			ekb.setPropertyBlackList(propertyBlackList);
			
			FallbackIndex fallback = new SolrIndex("http://dbpedia.aksw.org:8080/solr/dbpedia_resources");
			ekb.setFallbackIndex(fallback);
			
			ekb.setIcon(new ThemeResource("images/dbpedia_live_logo.png"));
			
			return ekb;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private ExtendedKnowledgebase createOxfordKnowledgebase(ExtractionDBCache cache){
			SparqlEndpoint endpoint = null;
			try {
				endpoint = new SparqlEndpoint(new URL("http://[2001:638:902:2010:0:168:35:138]/sparql"),
						Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
			SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
			SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
			
			
//			Model model = ModelFactory.createDefaultModel();
//			model.read(this.getClass().getClassLoader().getResourceAsStream("oxford-data.ttl"), null, "TURTLE");
//			Index resourcesIndex = new SPARQLIndex(model);
//			Index classesIndex = new SPARQLClassesIndex(model);
//			Index propertiesIndex = new SPARQLPropertiesIndex(model);
			
			
			
			MappingBasedIndex mappingIndex= new MappingBasedIndex(
					this.getClass().getClassLoader().getResource("oxford_class_mappings.txt").getPath(), 
					this.getClass().getClassLoader().getResource("oxford_resource_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("oxford_dataproperty_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("oxford_objectproperty_mappings.txt").getPath()
					);
			
//			Knowledgebase kb = new LocalKnowledgebase(model, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
			Knowledgebase kb = new RemoteKnowledgebase(endpoint, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
			
			
			String infoTemplateHtml = "<div><h3><b>label</b></h3></div>" +
        	 		"<div style='float: right; height: 100px; width: 200px'>" +
        	 		"<div style='height: 100%;'><img style='height: 100%;' src=\"imageURL\"/></div>" +
        	 		"</div>" +
        	 		"<div>description</div>";
			Map<String, String> propertiesMap = new HashMap<String, String>();
			propertiesMap.put("label", "http://purl.org/goodrelations/v1#name");
			propertiesMap.put("imageURL", "http://xmlns.com/foaf/0.1/depiction");
			propertiesMap.put("description", "http://purl.org/goodrelations/v1#description");
			InfoTemplate infoTemplate = new InfoTemplate(infoTemplateHtml, null);
			
			Map<String, String> optionalProperties = new HashMap<String, String>();
			optionalProperties.put("bedrooms", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#bedrooms");
			optionalProperties.put("bathrooms", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#bathrooms");
			optionalProperties.put("receptions", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#receptions");
			optionalProperties.put("street", "http://www.w3.org/2006/vcard/ns#street-address");
			optionalProperties.put("locality", "http://www.w3.org/2006/vcard/ns#locality");
			
			List<String> exampleQuestions = loadQuestions(this.getClass().getClassLoader().getResourceAsStream("oxford_example_questions.txt"));
			
			ExtendedKnowledgebase ekb = new ExtendedKnowledgebase(
					kb, 
					"http://purl.org/goodrelations/v1#name", 
					"http://purl.org/goodrelations/v1#description",
					"http://xmlns.com/foaf/0.1/depiction", null, optionalProperties, OxfordInfoLabel.class, "x0", false, exampleQuestions);
			FallbackIndex fallback = new LuceneIndex(oxfordFallbackIndexDir);
			ekb.setFallbackIndex(fallback);
			
			ekb.setIcon(new ThemeResource("images/oxford_logo.gif"));
			return ekb;
	}
	
	private List<String> loadQuestions(InputStream fileInputStream){
		List<String> questions = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
			String question;
			while((question = br.readLine()) != null){
				questions.add(question);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return questions;
	}
	
	private Set<String> loadPropertyBlackList(String filename){
		Set<String> uris = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filename)));
			String line;
			while((line = br.readLine()) != null){
				uris.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uris;
	}
	
	public static synchronized Manager getInstance(){
		if(instance == null){
			instance = new Manager();
		}
		return instance;
	}
	
	public PartOfSpeechTagger getPosTagger() {
		return posTagger;
	}
	
	public WordNet getWordNet() {
		return wordNet;
	}
	
	public String getWordnetDir() {
		return wordnetDir;
	}
	
	public String getCacheDir() {
		return cacheDir;
	}
	
	public String getSemMapURL() {
		return semMapURL;
	}
	
	public List<ExtendedKnowledgebase> getKnowledgebases(ExtractionDBCache cache) {
		List<ExtendedKnowledgebase> knowledgebases = new ArrayList<ExtendedKnowledgebase>();
		knowledgebases.add(createOxfordKnowledgebase(cache));
		knowledgebases.add(createDBpediaKnowledgebase(cache));
		return knowledgebases;
	}

}
