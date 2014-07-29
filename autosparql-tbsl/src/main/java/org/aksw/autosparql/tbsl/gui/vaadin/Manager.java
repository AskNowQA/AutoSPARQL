package org.aksw.autosparql.tbsl.gui.vaadin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.knowledgebase.DBpediaKnowledgebase;
import org.aksw.autosparql.commons.knowledgebase.OxfordKnowledgebase;
import org.aksw.autosparql.commons.nlp.pos.PartOfSpeechTagger;
import org.aksw.autosparql.commons.nlp.pos.StanfordPartOfSpeechTagger;
import org.aksw.autosparql.commons.nlp.wordnet.WordNet;
import org.aksw.autosparql.tbsl.gui.vaadin.model.ExtendedTBSL;
import org.aksw.autosparql.tbsl.gui.vaadin.model.InfoTemplate;
import org.aksw.autosparql.tbsl.gui.vaadin.util.FallbackIndex;
import org.aksw.autosparql.tbsl.gui.vaadin.util.SolrIndex;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.DBpediaInfoLabel;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.OxfordInfoLabel;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vaadin.terminal.ThemeResource;

public class Manager {
	private static final String SOLR_SERVER_URI_EN	= "http://solr.aksw.org/en_";	
	private static final Logger logger = Logger.getLogger(TBSLManager.class);

	private static Manager instance;

	private PartOfSpeechTagger posTagger;
	private WordNet wordNet;
	private List<ExtendedTBSL> knowledgebases;

	private String wordnetDir;
	private String cacheDir;
	private String oxfordFallbackIndexDir;
	private String semMapURL;

	private Manager() {
	}

	public void init(){
		logger.info("Initializing global settings...");
		loadSettings();

		posTagger = StanfordPartOfSpeechTagger.INSTANCE;
		//		wordNet = new WordNet(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
		wordNet = WordNet.INSTANCE;
		logger.info("...done.");
	}

	private void loadSettings(){
		InputStream is;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream("settings.ini");
			Ini ini = new Ini(is);
			//base section
			Section baseSection = ini.get("base");
			//			cacheDir = baseSection.get("cacheDir", String.class);
			cacheDir = System.getProperty("java.io.tmpdir");
			wordnetDir = baseSection.get("wordnetDir", String.class);
			oxfordFallbackIndexDir = baseSection.get("oxfordFallbackIndexDir", String.class);
			semMapURL = baseSection.get("SemMapURL", String.class);

		} catch (InvalidFileFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

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

	public List<ExtendedTBSL> getKnowledgebases(ExtractionDBCache cache) {
		List<ExtendedTBSL> knowledgebases = new ArrayList<ExtendedTBSL>();
		knowledgebases.add(ExtendedTBSL.DBPEDIA);
		knowledgebases.add(ExtendedTBSL.OXFORD);
		return knowledgebases;
	}

}