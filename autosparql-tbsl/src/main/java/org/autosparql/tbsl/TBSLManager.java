package org.autosparql.tbsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.sparql2nl.naturallanguagegeneration.SimpleNLGwithPostprocessing;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.autosparql.tbsl.model.Answer;
import org.autosparql.tbsl.model.BasicResultItem;
import org.autosparql.tbsl.model.ExtendedKnowledgebase;
import org.autosparql.tbsl.model.InfoTemplate;
import org.autosparql.tbsl.model.Refinement;
import org.autosparql.tbsl.model.SelectAnswer;
import org.autosparql.tbsl.util.FallbackIndex;
import org.autosparql.tbsl.util.LuceneIndex;
import org.autosparql.tbsl.util.SolrIndex;
import org.autosparql.tbsl.widget.DBpediaInfoLabel;
import org.autosparql.tbsl.widget.OxfordInfoLabel;
import org.autosparql.tbsl.widget.TBSLProgressListener;
import org.dllearner.algorithm.qtl.QTL;
import org.dllearner.algorithm.qtl.exception.EmptyLGGException;
import org.dllearner.algorithm.qtl.exception.NegativeTreeCoverageExecption;
import org.dllearner.algorithm.qtl.exception.TimeOutException;
import org.dllearner.algorithm.qtl.filters.QuestionBasedStatementFilter2;
import org.dllearner.algorithm.qtl.util.SPARQLEndpointEx;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner2;
import org.dllearner.algorithm.tbsl.nlp.ApachePartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.algorithm.tbsl.util.PopularityMap;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.MappingBasedIndex;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.VirtuosoClassesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.dllearner.common.index.VirtuosoResourcesIndex;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;
import org.ini4j.Profile.Section;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Lang;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCountVarDistinct;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vaadin.terminal.ThemeResource;

public class TBSLManager {
	
	private PartOfSpeechTagger posTagger;
	private WordNet wordNet;
	private String wordnetDir;
	private String cacheDir;
	private String oxfordFallbackIndexDir;
	
	private ExtractionDBCache cache = new ExtractionDBCache("cache");
	private ExtendedKnowledgebase currentExtendedKnowledgebase;
	
	private SPARQLTemplateBasedLearner2 tbsl;
	private FallbackIndex fallback;
	
	private String learnedSPARQLQuery;
	
	private Map<String, BasicResultItem> uri2Item;
	
	private Set<String> dataProperties = new HashSet<String>();
	
	private SimpleNLGwithPostprocessing nlg;
	
	private TBSLProgressListener progressListener;
	
	private Map<String, Map<String, Set<Object>>> property2URI2Values;
	private String currentQuestion;
	
	
	private List<ExtendedKnowledgebase> knowledgebases = new ArrayList<ExtendedKnowledgebase>();
	
	
	public TBSLManager() {
		posTagger = new ApachePartOfSpeechTagger();
		wordNet = new WordNet(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
		
		loadSettings();
		init();
	}
	
	public void init(){
		try {
			cache = new ExtractionDBCache(cacheDir);
			
			knowledgebases.add(createOxfordKnowledgebase());
			knowledgebases.add(createDBpediaKnowledgebase());
			
			currentExtendedKnowledgebase = knowledgebases.get(0);
			
			tbsl = new SPARQLTemplateBasedLearner2(currentExtendedKnowledgebase.getKnowledgebase(), posTagger, wordNet, new Options());
			tbsl.setMappingIndex(currentExtendedKnowledgebase.getKnowledgebase().getMappingIndex());
			tbsl.init();
			
			nlg = new SimpleNLGwithPostprocessing(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint(), wordnetDir);
		}  catch (ComponentInitException e) {
			e.printStackTrace();
		} 
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
		
		} catch (InvalidFileFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
	}
	
	private Set<String> loadBlackList(String filename){
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
	
	private ExtendedKnowledgebase createDBpediaKnowledgebase(){
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
			
			Knowledgebase kb = new Knowledgebase(endpoint, "DBpedia Live", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
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
			
			Set<String> propertyBlackList = loadBlackList("dbpedia_property_blacklist.txt");
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
	
	public void setProgressListener(TBSLProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public String getNLRepresentation(String sparqlQueryString){
		return translateSPARQLQuery(sparqlQueryString);
	}
	
	private String translateSPARQLQuery(String sparqlQueryString){
		return translateSPARQLQuery(QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ));
	}
	
	private String translateSPARQLQuery(Query sparqlQuery){
		return nlg.getNLR(sparqlQuery);
	}
	
	private ExtendedKnowledgebase createOxfordKnowledgebase(){
		try {
			SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
			
			SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
			SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
			SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
			MappingBasedIndex mappingIndex= new MappingBasedIndex(
					this.getClass().getClassLoader().getResource("oxford_class_mappings.txt").getPath(), 
					this.getClass().getClassLoader().getResource("oxford_resource_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("oxford_dataproperty_mappings.txt").getPath(),
					this.getClass().getClassLoader().getResource("oxford_objectproperty_mappings.txt").getPath()
					);
			
			Knowledgebase kb = new Knowledgebase(endpoint, "Oxford - Real estate", "TODO", resourcesIndex, propertiesIndex, classesIndex, mappingIndex);
			
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Refinement refine(List<String> posExamples, List<String> negExamples){
		QTL qtl = new QTL(new SPARQLEndpointEx(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint(), null, null, Collections.<String>emptySet()),
				cache);
		qtl.setRestrictToNamespaces(currentExtendedKnowledgebase.getPropertyNamespaces());
		Set<String> relevantKeywords = tbsl.getRelevantKeywords();
		System.out.println("Relevant keywords: " + relevantKeywords);
		qtl.addStatementFilter(new QuestionBasedStatementFilter2(relevantKeywords));
		try {
			String example = qtl.getQuestion(posExamples, negExamples);
			String refinedSPARQLQuery = qtl.getBestSPARQLQuery();
			List<BasicResultItem> result = fetchResult(refinedSPARQLQuery);
			Map<String, Integer> additionalProperties = getAdditionalProperties();
			
			List<String> mostProminentProperties = new ArrayList<String>();
			List<Entry<String, Integer>> sortedByValues = sortByValues(additionalProperties);
			for(int i = 0; i < Math.min(sortedByValues.size(), 5); i++){
				String propertyURI = sortedByValues.get(i).getKey();
				mostProminentProperties.add(propertyURI);
				additionalProperties.remove(propertyURI);
			}
			fillItems(mostProminentProperties);
			Refinement refinement = new Refinement(posExamples, negExamples, learnedSPARQLQuery, refinedSPARQLQuery, new SelectAnswer(result, mostProminentProperties, additionalProperties), example);
			return refinement;
		} catch (EmptyLGGException e) {
			e.printStackTrace();
		} catch (NegativeTreeCoverageExecption e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeOutException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ExtendedKnowledgebase> getKnowledgebases() {
		return knowledgebases;
	}
	
	public void setKnowledgebase(ExtendedKnowledgebase ekb){
		this.currentExtendedKnowledgebase = ekb;
		tbsl.setKnowledgebase(ekb.getKnowledgebase());
		if(ekb.getInfoBoxClass() == OxfordInfoLabel.class){
			tbsl.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
		} else {
			tbsl.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex"});
			PopularityMap map = new PopularityMap(
					this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(), currentExtendedKnowledgebase.getKnowledgebase().getEndpoint(), cache);
			map.init();
			tbsl.setPopularityMap(map);
		}
		nlg = new SimpleNLGwithPostprocessing(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint(), "/opt/wordnet/dict");
		
		fallback = ekb.getFallbackIndex();
	}
	
	public ExtendedKnowledgebase getCurrentExtendedKnowledgebase() {
		return currentExtendedKnowledgebase;
	}
	
//	public List<BasicResultItem> answerQuestion(String question){
//		learnedSPARQLQuery = null;
//		List<BasicResultItem> result = new ArrayList<BasicResultItem>();
//		try {
//			tbsl.setQuestion(question);
//			tbsl.learnSPARQLQueries();
//			
//			learnedSPARQLQuery = tbsl.getBestSPARQLQuery();
//			Query q = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
//			if(!q.hasGroupBy()){
//				q.setDistinct(true);
//				learnedSPARQLQuery = q.toString();
//			}
//			System.out.println("Learned SPARQL Query:\n" + learnedSPARQLQuery);
//			String extendedSPARQLQuery = extendSPARQLQuery(learnedSPARQLQuery);
//			System.out.println("Extended SPARQL Query:\n" + extendedSPARQLQuery);
//			
//			ResultSet rs = executeSelect(extendedSPARQLQuery);
//			QuerySolution qs;
//			while(rs.hasNext()){
//				qs = rs.next();
//				
//				String uri = qs.getResource(currentExtendedKnowledgebase.getTargetVar()).getURI();
//				String label = qs.getLiteral("label").getLexicalForm();
//				String description = qs.getLiteral("desc").getLexicalForm();
//				String imageURL = null;
//				RDFNode imgNode = qs.get("img");
//				if(imgNode != null){
//					if(imgNode.isLiteral()){
//						imageURL = imgNode.asLiteral().getLexicalForm();
//					} else if(imgNode.isURIResource()){
//						imageURL = imgNode.asResource().getURI();
//					}
//				}
//				Map<String, Object> data = new HashMap<String, Object>();
//				Map<String, String> optionalProperties = currentExtendedKnowledgebase.getOptionalProperties();
//				if(optionalProperties != null){
//					for(Entry<String, String> entry : optionalProperties.entrySet()){
//						RDFNode node = qs.get(entry.getKey());
//						if(node != null){
//							if(node.isURIResource()){
//								data.put(entry.getKey(), node.asResource().getURI());
//							} else if(node.isLiteral()){
//								Object value;
//								Literal lit = node.asLiteral();
//								if(lit.getDatatype() == XSD.INT){
//									value = lit.getInt();
//								} else if(lit.getDatatype() == XSD.DOUBLE){
//									value = lit.getDouble();
//								} else {
//									value = lit.getLexicalForm();
//								}
//								data.put(entry.getKey(), value);
//							}
//						}
//					}
//				}
//				//Oxford price relation
//				if(currentExtendedKnowledgebase.getInfoBoxClass() == OxfordInfoLabel.class){
//					data.put("price", qs.getLiteral("price").getDouble());
//				}
//				result.add(new BasicResultItem(uri, label, description, imageURL, data));
//			}
//		} catch (NoTemplateFoundException e) {
//			e.printStackTrace();
////			result.addAll(fallback.getResources(question));
//		}
//		
//		return result;
//	}
	
	private void message(String message){
		if(progressListener != null){
			progressListener.message(message);
		}
	}
	
	public Answer answerQuestion(String question){
		this.currentQuestion = question;
		Answer answer = null;
		try {
			uri2Item = new HashMap<String, BasicResultItem>();
			property2URI2Values = new HashMap<String, Map<String,Set<Object>>>();
			message("Running...");
			learnedSPARQLQuery = null;
			tbsl.setQuestion(question);
			tbsl.learnSPARQLQueries();
			learnedSPARQLQuery = tbsl.getBestSPARQLQuery();
			
			if(learnedSPARQLQuery != null){
				String translatedQuery = getNLRepresentation(learnedSPARQLQuery);
				translatedQuery = translatedQuery.replace("This query retrieves", "").replace("distinct", "").replace(".", "").trim();
				message("Found answer for \"" + translatedQuery + "\"");
				Query q = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
				if(!q.hasGroupBy()){
					q.setDistinct(true);
					learnedSPARQLQuery = q.toString();
				}
				System.out.println("Learned SPARQL Query:\n" + learnedSPARQLQuery);
				
				if(q.isSelectType()){
					
					List<BasicResultItem> result = fetchResult(learnedSPARQLQuery);
					Map<String, Integer> additionalProperties = getAdditionalProperties();
					
					List<String> mostProminentProperties = new ArrayList<String>();
					List<Entry<String, Integer>> sortedByValues = sortByValues(additionalProperties);
					for(int i = 0; i < Math.min(sortedByValues.size(), 5); i++){
						String propertyURI = sortedByValues.get(i).getKey();
						mostProminentProperties.add(propertyURI);
						additionalProperties.remove(propertyURI);
					}
					fillItems(mostProminentProperties);
					answer = new SelectAnswer(result, mostProminentProperties, additionalProperties);
				} else if(q.isAskType()){
					
				}
			} else {
				message("Could not find an answer. Using fallback by searching in descriptions of the entities.");
				answer = answerQuestionFallback(question);
			}
			
			
			
			
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
			answer = answerQuestionFallback(question);
		}
//		message("Finished.");
		if(progressListener != null){
			progressListener.finished(answer);
		}
		
		return answer;
	}
	
	private List<BasicResultItem> fetchResult(String query){
		Query extendedSPARQLQuery = extendSPARQLQuery(learnedSPARQLQuery);
		System.out.println("Loading result...");
//		message("Loading result");
		List<BasicResultItem> result = new ArrayList<BasicResultItem>();
		ResultSet rs = executeSelect(extendedSPARQLQuery.toString());
		QuerySolution qs;
		String targetVar = currentExtendedKnowledgebase.getTargetVar();
		targetVar = extendedSPARQLQuery.getProjectVars().get(0).getVarName();
		while(rs.hasNext()){
			qs = rs.next();
			
			String uri = null;
			RDFNode targetNode = qs.get(targetVar);
			if(targetNode.isURIResource()){
				uri = targetNode.asResource().getURI();
			} else if(targetNode.isLiteral()){
				uri = targetNode.asLiteral().getLexicalForm();
			}
			
			//get the label
			String label = null;
			RDFNode node = qs.get("label");
			if(node != null){
				label = qs.getLiteral("label").getLexicalForm();
			}
			//get the description
			String description = null;
			node = qs.get("desc");
			if(node != null){
				description = qs.getLiteral("desc").getLexicalForm();
			}
			
			String imageURL = null;
			RDFNode imgNode = qs.get("img");
			if(imgNode != null){
				if(imgNode.isLiteral()){
					imageURL = imgNode.asLiteral().getLexicalForm();
				} else if(imgNode.isURIResource()){
					imageURL = imgNode.asResource().getURI();
				}
			}
			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, String> optionalProperties = currentExtendedKnowledgebase.getOptionalProperties();
			if(optionalProperties != null){
				for(Entry<String, String> entry : optionalProperties.entrySet()){
					node = qs.get(entry.getKey());
					if(node != null){
						if(node.isURIResource()){
							data.put(entry.getKey(), node.asResource().getURI());
						} else if(node.isLiteral()){
							dataProperties.add(entry.getKey());
							Object value;
							Literal lit = node.asLiteral();
							RDFDatatype dt = lit.getDatatype();
							if(dt != null){
								if(dt == XSDDatatype.XSDint || dt == XSDDatatype.XSDinteger){
									value = lit.getInt();
								} else if(dt == XSDDatatype.XSDdouble){
									value = lit.getDouble();
								} else {
									value = lit.getLexicalForm();
								}
								
							} else {
								value = lit.getLexicalForm();
							}
							data.put(entry.getKey(), value);
						}
					}
				}
			}
			
			//Oxford price relation
			if(currentExtendedKnowledgebase.getInfoBoxClass() == OxfordInfoLabel.class){
				Literal priceLit = qs.getLiteral("price");
				Double price = null;
				try {
					price = qs.getLiteral("price").getDouble();
				} catch (Exception e) {
					e.printStackTrace();
					NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
					try {
						price = format.parse(priceLit.getLexicalForm()).doubleValue();
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				data.put("price", price);
			}
			BasicResultItem item = new BasicResultItem(uri, label, description, imageURL, data);
			result.add(item);
			
			uri2Item.put(uri, item);
			
		}
		return result;
	}
	
	
	public String getAnswerAsSPARQLQuery(String question){
		learnedSPARQLQuery = null;
		tbsl.setQuestion(question);
		try {
			tbsl.learnSPARQLQueries();
			learnedSPARQLQuery = tbsl.getBestSPARQLQuery();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		return learnedSPARQLQuery;
	}
	
	private Answer answerQuestionFallback(String question){
		System.out.println("Using fallback.");
		
		List<BasicResultItem> result = fallback.getData(question, 100, 0);
		
		Map<String, Integer> additionalProperties = getAdditionalProperties();
		
		return new SelectAnswer(result, Collections.<String>emptyList(), additionalProperties);
	}
	
	public boolean isDataProperty(String propertyURI){
		return dataProperties.contains(propertyURI);
	}
	
	public Map<String, Integer> getAdditionalProperties(){
		Map<String, Integer> properties = new HashMap<String, Integer>();
		if(currentExtendedKnowledgebase.isAllowAdditionalProperties() && learnedSPARQLQuery !=null){
			System.out.println("Loading additional,common properties");
			Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
			ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
			ElementPathBlock pb = null;
			for(Element el : wherePart.getElements()){
				if(el instanceof ElementPathBlock){
					pb = (ElementPathBlock) el;
					break;
				}
			}
			
			String targetVar = currentExtendedKnowledgebase.getTargetVar();
			targetVar = extendedSPARQLQuery.getProjectVars().get(0).getVarName();
			
			Query newQuery = QueryFactory.create();
			newQuery.setSyntax(Syntax.syntaxARQ);
			newQuery.setDistinct(true);
			newQuery.setQuerySelectType();
			pb.addTriple(new Triple(Node.createVariable(targetVar), Node.createVariable("prop"), Node.createVariable("value")));
			newQuery.setQueryPattern(wherePart);
			wherePart.addElementFilter(new ElementFilter(new E_Regex(new E_Str(new ExprVar("prop")), "http://dbpedia.org/ontology/", "")));
			List<String> vars = new ArrayList<String>();
			vars.add("prop");
			newQuery.addProjectVars(vars);
			//add COUNT ?x0 and GROUP BY ?prop
			Expr count = newQuery.allocAggregate(new AggCountVarDistinct(new ExprVar(Node.createVariable(targetVar))));
			newQuery.addResultVar("cnt", count);
			newQuery.addGroupBy(new ExprVar("prop"));
			
			ResultSet rs = executeSelect(newQuery.toString());
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				String propertyURI = qs.getResource("prop").getURI();
				int cnt = qs.getLiteral("cnt").getInt();
				if(!currentExtendedKnowledgebase.getPropertyBlackList().contains(propertyURI)){
					properties.put(propertyURI, cnt);
				}
			}
		}
		return properties;
	}
	
	public void fillItems(String propertyURI){
		System.out.println("Filling data with " + propertyURI);
		Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
		ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
		ElementPathBlock pb = null;
		for(Element el : wherePart.getElements()){
			if(el instanceof ElementPathBlock){
				pb = (ElementPathBlock) el;
				break;
			}
		}
		String targetVar = currentExtendedKnowledgebase.getTargetVar();
		targetVar = extendedSPARQLQuery.getProjectVars().get(0).getVarName();
		
		pb.addTriple(new Triple(Node.createVariable(targetVar), Node.createURI(propertyURI), Node.createVariable("value")));
		List<String> vars = new ArrayList<String>();
		vars.add("value");
		extendedSPARQLQuery.addProjectVars(vars);
		
		
		ResultSet rs = executeSelect(extendedSPARQLQuery.toString());
		QuerySolution qs;
		Map<String, Set<Object>> uri2Values = new HashMap<String, Set<Object>>();
		while(rs.hasNext()){
			qs = rs.next();
			String uri = qs.getResource(targetVar).getURI();
			Set<Object> values = uri2Values.get(uri);
			if(values == null){
				values = new HashSet<Object>();
				uri2Values.put(uri, values);
			}
			Object value = null;
			RDFNode node = qs.get("value");
			if(node.isURIResource()){
				value = node.asResource().getURI();
			} else if(node.isLiteral()){
				dataProperties.add(propertyURI);
				Literal lit = node.asLiteral();
				RDFDatatype dt = lit.getDatatype();
				if(dt != null){
					if(dt == XSDDatatype.XSDint || dt == XSDDatatype.XSDinteger){
						value = lit.getInt();
					} else if(dt == XSDDatatype.XSDdouble){
						value = lit.getDouble();
					} else {
						value = lit.getLexicalForm();
					}
					
				} else {
					value = lit.getLexicalForm();
				}
			}
			values.add(value);
			
			
		}
		for(Entry<String, Set<Object>> entry : uri2Values.entrySet()){
			uri2Item.get(entry.getKey()).getData().put(propertyURI, entry.getValue());
		}
		property2URI2Values.put(propertyURI, uri2Values);
		
	}
	
	public void fillItems(List<String> propertyURIs){
		System.out.println("Filling data with " + propertyURIs);
		Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
		ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
		ElementPathBlock pb = null;
		for(Element el : wherePart.getElements()){
			if(el instanceof ElementPathBlock){
				pb = (ElementPathBlock) el;
				break;
			}
		}
		String targetVar = currentExtendedKnowledgebase.getTargetVar();
		targetVar = extendedSPARQLQuery.getProjectVars().get(0).getVarName();
		List<String> vars = new ArrayList<String>();
		int i = 0;
		boolean useOptional = true;//we have to put each triple into an OPTIONAL construct, because otherwise only information for resources having all properties will be returned
		for(String uri : propertyURIs){
			Triple t = new Triple(Node.createVariable(targetVar), Node.createURI(uri), Node.createVariable("value" + i));
			if(useOptional){
				ElementGroup eg = new ElementGroup();
				eg.addTriplePattern(t);
				ElementOptional optionalEl = new ElementOptional(eg);
				wherePart.addElement(optionalEl);
			} else {
				pb.addTriple(t);
			}
			vars.add("value" + i);
			i++;
		}
		extendedSPARQLQuery.addProjectVars(vars);
		
		Map<String, Map<String, Set<Object>>> uri2Property2Values = new HashMap<String, Map<String,Set<Object>>>();
		ResultSet rs = executeSelect(extendedSPARQLQuery.toString());
		QuerySolution qs;
		while(rs.hasNext()){
			qs = rs.next();
			
			String uri = qs.getResource(targetVar).getURI();
			for(i = 0; i < propertyURIs.size(); i++){
				Object value = null;
				RDFNode node = qs.get("value" + i);
				if(node != null){
					String propertyURI = propertyURIs.get(i);
					Map<String, Set<Object>> property2Values = uri2Property2Values.get(uri);
					if(property2Values == null){
						property2Values = new HashMap<String, Set<Object>>();
						uri2Property2Values.put(uri, property2Values);
					}
					Set<Object> values = property2Values.get(propertyURI);
					if(values == null){
						values = new HashSet<Object>();
						property2Values.put(propertyURI, values);
					}
					if(node.isURIResource()){
						value = node.asResource().getURI();
					} else if(node.isLiteral()){
						dataProperties.add(propertyURI);
						Literal lit = node.asLiteral();
						RDFDatatype dt = lit.getDatatype();
						if(dt != null){
							if(dt == XSDDatatype.XSDint || dt == XSDDatatype.XSDinteger){
								value = lit.getInt();
							} else if(dt == XSDDatatype.XSDdouble){
								value = lit.getDouble();
							} else {
								value = lit.getLexicalForm();
							}
							
						} else {
							value = lit.getLexicalForm();
						}
					}
					values.add(value);
				}
			}
		}
		for (Entry<String, Map<String, Set<Object>>> entry : uri2Property2Values.entrySet()) {
			String uri = entry.getKey();
			for(Entry<String, Set<Object>> prop2Values : entry.getValue().entrySet()){
				String propertyURI = prop2Values.getKey();
				Set<Object> values = prop2Values.getValue();
				uri2Item.get(uri).getData().put(propertyURI, values);
				
				Map<String, Set<Object>> uri2Values = property2URI2Values.get(propertyURI);
				if(uri2Values == null){
					uri2Values = new HashMap<String, Set<Object>>();
					property2URI2Values.put(propertyURI, uri2Values);
				}
				uri2Values.put(uri, values);
			}
		}
		
	}
	
	public Map<String, Set<Object>> getDataForProperty(String propertyURI){
		return property2URI2Values.get(propertyURI);
	}
	
	public String getCurrentQuestion() {
		return currentQuestion;
	}
	
	public XSDDatatype getDatatype(String propertyURI){
		String sparqlQuery = String.format("SELECT ?range WHERE {<%s> <http://www.w3.org/2000/01/rdf-schema#range> ?range}", propertyURI);
		ResultSet rs = executeSelect(sparqlQuery);
		if(rs.hasNext()){
			String datatypeURI = rs.next().get("range").asResource().getURI();
			if(datatypeURI.equals(XSDDatatype.XSDdouble.getURI())){
				return XSDDatatype.XSDdouble;
			} else if(datatypeURI.equals(XSDDatatype.XSDdate.getURI())){
				return XSDDatatype.XSDdate;
			} else if(datatypeURI.equals(XSDDatatype.XSDdateTime.getURI())){
				return XSDDatatype.XSDdateTime;
			} else if(datatypeURI.equals(XSDDatatype.XSDint.getURI())){
				return XSDDatatype.XSDint;
			} else if(datatypeURI.equals(XSDDatatype.XSDinteger.getURI())){
				return XSDDatatype.XSDinteger;
			} else if(datatypeURI.equals(XSDDatatype.XSDfloat.getURI())){
				return XSDDatatype.XSDfloat;
			} else if(datatypeURI.equals(XSDDatatype.XSDpositiveInteger.getURI())){
				return XSDDatatype.XSDpositiveInteger;
			} else if(datatypeURI.equals(XSDDatatype.XSDnegativeInteger.getURI())){
				return XSDDatatype.XSDnegativeInteger;
			}
			//dbpedia specific
			else if(datatypeURI.equals("http://dbpedia.org/ontology/Time")){
				return XSDDatatype.XSDdouble;
			} 
		} 
		//infer datatype by sample values
		Set<Object> values = property2URI2Values.get(propertyURI).entrySet().iterator().next().getValue();
		Object value = values.iterator().next();
		if(value instanceof Double){
			return XSDDatatype.XSDdouble;
		} else if(value instanceof Integer){
			return XSDDatatype.XSDinteger;
		} else if(value instanceof String){
			try {
				Double.parseDouble((String) value);
				return XSDDatatype.XSDdouble;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				try {
					Integer.parseInt((String) value);
					return XSDDatatype.XSDinteger;
				} catch (NumberFormatException e2) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private Query extendSPARQLQuery(String sparqlQuery){
		Query extendedSPARQLQuery = QueryFactory.create(sparqlQuery, Syntax.syntaxARQ);
		ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
		ElementPathBlock pb = null;
		for(Element el : wherePart.getElements()){
			if(el instanceof ElementPathBlock){
				pb = (ElementPathBlock) el;
				break;
			}
		}
		
		String targetVar = currentExtendedKnowledgebase.getTargetVar();
		targetVar = extendedSPARQLQuery.getProjectVars().get(0).getVarName();
		List<String> vars = new ArrayList<String>();
		
		
//		if(currentExtendedKnowledgebase.isAllowAdditionalProperties()){
//			pb.addTriple(new Triple(Node.createVariable(targetVar), Node.createVariable("prop"), Node.createVariable("value")));
//			wherePart.addElementFilter(new ElementFilter(new E_Regex(new E_Str(new ExprVar("prop")), "http://dbpedia.org/ontology/", "")));
//			vars.add("prop");
//			vars.add("value");
//		}
		//add label triples
		boolean optional = true;
		Triple triple = new Triple(Node.createVariable(targetVar), Node.createURI(currentExtendedKnowledgebase.getLabelPropertyURI()), Node.createVariable("label"));
		ElementFilter filter = null;
		String lang = currentExtendedKnowledgebase.getLabelPropertyLanguage();
		if(lang != null){
			filter = new ElementFilter(new E_Equals(new E_Lang(new ExprVar("label")), new NodeValueString(lang)));
		}
		if(optional){
			ElementGroup eg = new ElementGroup();
			eg.addTriplePattern(triple);
			if(filter != null){
				eg.addElementFilter(filter);
			}
			ElementOptional optionalEl = new ElementOptional(eg);
			wherePart.addElement(optionalEl);
		} else {
			pb.addTriple(triple);
			if(filter != null){
				wherePart.addElementFilter(filter);
			}
			
		}
		vars.add("label");
		//add description/comment triples
		triple = new Triple(Node.createVariable(targetVar), Node.createURI(currentExtendedKnowledgebase.getDescriptionPropertyURI()), Node.createVariable("desc"));
		filter = null;
		lang = currentExtendedKnowledgebase.getLabelPropertyLanguage();
		if(lang != null){
			filter = new ElementFilter(new E_Equals(new E_Lang(new ExprVar("desc")), new NodeValueString(lang)));
		}
		if(optional){
			ElementGroup eg = new ElementGroup();
			eg.addTriplePattern(triple);
			if(filter != null){
				eg.addElementFilter(filter);
			}
			ElementOptional optionalEl = new ElementOptional(eg);
			wherePart.addElement(optionalEl);
		} else {
			pb.addTriple(triple);
			if(filter != null){
				wherePart.addElementFilter(filter);
			}
			
		}
		vars.add("desc");
		//add image triples
		if(currentExtendedKnowledgebase.getImagePropertyURI() != null){
			Triple imgTriple = new Triple(Node.createVariable(targetVar), Node.createURI(currentExtendedKnowledgebase.getImagePropertyURI()), Node.createVariable("img"));
			if(optional){
				ElementGroup eg = new ElementGroup();
				eg.addTriplePattern(imgTriple);
				ElementOptional optionalEl = new ElementOptional(eg);
				wherePart.addElement(optionalEl);
			} else {
				pb.addTriple(imgTriple);
				
			}
			vars.add("img");
		}
		//hack to get price in Oxford data
		if(currentExtendedKnowledgebase.getInfoBoxClass() == OxfordInfoLabel.class){
			pb.addTriple(new Triple(Node.createVariable("offer"), Node.createURI("http://purl.org/goodrelations/v1#includes"), Node.createVariable(targetVar)));
			pb.addTriple(new Triple(Node.createVariable("offer"), Node.createURI("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice"), Node.createVariable("price")));
			vars.add("price");
		}
		Map<String, String> mandatoryProperties = currentExtendedKnowledgebase.getMandatoryProperties();
		if(mandatoryProperties != null){
			
		}
		Map<String, String> optionalProperties = currentExtendedKnowledgebase.getOptionalProperties();
		if(optionalProperties != null){
			for(Entry<String, String> entry : optionalProperties.entrySet()){
				ElementGroup eg = new ElementGroup();
				eg.addTriplePattern(new Triple(Node.createVariable(targetVar), Node.createURI(entry.getValue()), Node.createVariable(entry.getKey())));
				ElementOptional optionalEl = new ElementOptional(eg);
				wherePart.addElement(optionalEl);
				vars.add(entry.getKey());
			}
		}
		extendedSPARQLQuery.addProjectVars(vars);
		return extendedSPARQLQuery;
	}
	
	private ResultSet executeSelect(String sparqlQuery){
		System.out.println("Sending query\n" + sparqlQuery);
		ResultSet rs;
		if (cache == null) {
			QueryEngineHTTP qe = new QueryEngineHTTP(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint().getURL().toString(), sparqlQuery);
			qe.setDefaultGraphURIs(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint().getDefaultGraphURIs());
			rs = qe.execSelect();
		} else {
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(currentExtendedKnowledgebase.getKnowledgebase().getEndpoint(), sparqlQuery));
		}
		
		return rs;
	}
	
	public String getLearnedSPARQLQuery() {
		return learnedSPARQLQuery;
	}
	
	private List<Entry<String, Integer>> sortByValues(Map<String, Integer> map){
		List<Entry<String, Integer>> entries = new ArrayList<Entry<String, Integer>>(map.entrySet());
		
        Collections.sort(entries, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				int ret = o2.getValue().compareTo(o1.getValue());
				if(ret == 0){
					ret = o2.getKey().compareTo(o1.getKey());
				}
				return ret; 
			}
		});
        return entries;
	}
	
	public static void main(String[] args) {
		char ch = '\uD83D\uDCCA';
		System.out.println(ch);
		Logger.getLogger(QTL.class).setLevel(Level.DEBUG);
		TBSLManager man = new TBSLManager();
		man.init();
		man.setKnowledgebase(man.getKnowledgebases().get(0));
		SelectAnswer a = (SelectAnswer) man.answerQuestion("houses with double garage");
		List<String> p = new ArrayList<String>();
		p.add(a.getItems().get(1).getUri());
		p.add(a.getItems().get(2).getUri());
		p.add(a.getItems().get(0).getUri());
		List<String> n = new ArrayList<String>();
		man.refine(p, n);
	}

}
