package org.aksw.autosparql.tbsl.gui.vaadin;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.NoTemplateFoundException;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.gui.vaadin.model.Answer;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;
import org.aksw.autosparql.tbsl.gui.vaadin.model.ExtendedTBSL;
import org.aksw.autosparql.tbsl.gui.vaadin.model.Refinement;
import org.aksw.autosparql.tbsl.gui.vaadin.model.SelectAnswer;
import org.aksw.autosparql.tbsl.gui.vaadin.util.FallbackIndex;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.OxfordInfoLabel;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.TBSLProgressListener;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.QTL;
import org.dllearner.algorithms.qtl.exception.EmptyLGGException;
import org.dllearner.algorithms.qtl.exception.NegativeTreeCoverageExecption;
import org.dllearner.algorithms.qtl.exception.TimeOutException;
import org.dllearner.algorithms.qtl.filters.QuestionBasedStatementFilter2;
import org.dllearner.algorithms.qtl.util.SPARQLEndpointEx;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.eclipse.jetty.util.log.Log;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
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

/** Manages TBSL and is called by the main view. */
public class TBSLManager
{
//	public enum SELECTED_TBSL {DBPEDIA,OXFORD};
	
	private final Logger logger = Logger.getLogger(TBSLManager.class);
	
	public ExtendedTBSL activeTBSL = ExtendedTBSL.OXFORD;
	public ExtendedTBSL[] tbsls = {ExtendedTBSL.OXFORD,ExtendedTBSL.DBPEDIA};
	
	private FallbackIndex fallback;
	
	private String learnedSPARQLQuery;
	
	private Map<String, BasicResultItem> uri2Item;
	
	private Set<String> dataProperties = new HashSet<String>();	
	
	private TBSLProgressListener progressListener;
	
	private Map<String, Map<String, Set<Object>>> property2URI2Values;
	private String currentQuestion;
	
	final ExtractionDBCache cache;
	
	public TBSLManager()
	{
		cache = new ExtractionDBCache(Manager.getInstance().getCacheDir());
		cache.setMaxExecutionTimeInSeconds(100);
	}
	
	public void setProgressListener(TBSLProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public Map<String, BasicResultItem> getUri2Items() {
		return uri2Item;
	}
	
	public String getNLRepresentation(String sparqlQueryString){
		String translatedQuery = translateSPARQLQuery(sparqlQueryString);
		translatedQuery = translatedQuery.replace("This query retrieves", "").replace("distinct", "").replace(".", "").replace("(ignoring case)","").trim();
		translatedQuery = normalizeVarNames(translatedQuery);
		return translatedQuery;
	}
	
	private String translateSPARQLQuery(String sparqlQueryString){
		return translateSPARQLQuery(QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ));
	}
	
	private String translateSPARQLQuery(Query sparqlQuery){
		logger.debug("Translating query: "+sparqlQuery);		
		return activeTBSL.nlg.getNLR(sparqlQuery);
	}
	
	
	/** Used after getting answers from a query. 
	 * @param posExamples
	/** @param negExamples
	/** @return */
	public Refinement refine(List<String> posExamples, List<String> negExamples){
		logger.info("Refining answer...");
		logger.info("Positive examples: " + posExamples);
		logger.info("Negative examples: " + negExamples);
		QTL qtl;
		Knowledgebase kb = getActiveTBSL().getTBSL().getKnowledgebase();
		if(kb instanceof RemoteKnowledgebase){
			
			SPARQLEndpointEx endpoint = new SPARQLEndpointEx(((RemoteKnowledgebase) kb).getEndpoint(), null, null, Collections.<String>emptySet());
//			qtl = new QTL(endpoint,cache);
			qtl = new QTL(endpoint,cache.getCacheDirectory());
		} else {
			qtl = new QTL(((LocalKnowledgebase) kb).getModel());
		}
				
		qtl.setRestrictToNamespaces(getActiveTBSL().getPropertyNamespaces());
		qtl.init();
		
//		Set<String> relevantKeywords = activeTBSL.getTBSL().ggetRelevantKeywords();
		logger.info("Relevant filter keywords: " + activeTBSL.getTBSL().getRelevantKeywords());
		qtl.addStatementFilter(new QuestionBasedStatementFilter2(activeTBSL.getTBSL().getRelevantKeywords()));
		try {
			String example = qtl.getQuestion(posExamples, negExamples);
			String refinedSPARQLQuery = qtl.getBestSPARQLQuery();
			if(refinedSPARQLQuery != null){
				logger.info("Refinement successful.");
				logger.info("Refined SPARQL query:\n" + refinedSPARQLQuery);
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
			}
			
		} catch (EmptyLGGException e) {
			logger.error("Empty LGG", e);
		} catch (NegativeTreeCoverageExecption e) {
			logger.error("Negative tree covered", e);
		} catch (TimeOutException e) {
			logger.error("QTL timeout", e);
		}
		return null;
	}
	
//	public List<ExtendedKnowledgebase> getKnowledgebases() {
//		return knowledgebases;
//	}
	
	public void setKnowledgebase(ExtendedTBSL ekb){
		this.setActiveTBSL(ekb);
//		
//		activeTBSL.setKnowledgebase(ekb.getKnowledgebase());
//		if(ekb.getInfoBoxClass() == OxfordInfoLabel.class){
//			try {
//				activeTBSL.init();
//			} catch (ComponentInitException e) {
//				e.printStackTrace();
//			}
//			activeTBSL.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
//			activeTBSL.setUseDomainRangeRestriction(false);
//			activeTBSL.setPopularityMap(null);
//		} else {
//			activeTBSL.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex"});
//			PopularityMap map;
//			Knowledgebase kb = currentExtendedKnowledgebase.getKnowledgebase();
//			if(kb instanceof RemoteKnowledgebase){
//				map = new PopularityMap(this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(),
//						 new SparqlQueriable(((RemoteKnowledgebase)kb).getEndpoint(), "cache"));
//			} else {
//				map = new PopularityMap(this.getClass().getClassLoader().getResource("dbpedia_popularity.map").getPath(),
//						new SparqlQueriable(((LocalKnowledgebase)kb).getModel()));
//			}			
//			activeTBSL.setUseDomainRangeRestriction(true);
//			activeTBSL.setPopularityMap(map);
//		}
//		Knowledgebase kb = currentExtendedKnowledgebase.getKnowledgebase();
//		if(kb instanceof RemoteKnowledgebase){
//			nlg = new SimpleNLGwithPostprocessing(((RemoteKnowledgebase) kb).getEndpoint(), Manager.getInstance().getWordnetDir());
//		} else {
//			nlg = new SimpleNLGwithPostprocessing(((LocalKnowledgebase) kb).getModel(), Manager.getInstance().getWordnetDir());
//		}
//		//		tbsl.setCache(cache);
//		fallback = ekb.getFallbackIndex();
	}
	
//	public ExtendedKnowledgebase getCurrentExtendedKnowledgebase() {
//		return currentExtendedKnowledgebase;
//	}
	
	public List<Entry<String, String>> getMoreSolutions(){
		return getMoreSolutions(1);
	}
	
	public List<Entry<String, String>> getMoreSolutions(int offset){
//		Map<String, String> query2Translation = new LinkedHashMap<String, String>();
//		
//		SortedSet<WeightedQuery> otherCandidates = activeTBSL.getGeneratedQueries();
//		List<WeightedQuery> subList = new ArrayList<WeightedQuery>(otherCandidates).subList(
//				offset, Math.min(otherCandidates.size(), offset + 10));
//		for(WeightedQuery wQ : subList){
//			String queryString = wQ.getQuery().toString();
//			String translation = getNLRepresentation(queryString);
//			query2Translation.put(queryString, translation);
//		}
		List<Entry<String, String>> otherSolutions = new ArrayList<Entry<String,String>>();
//		for(Entry<String, String> entry : query2Translation.entrySet()){
//			otherSolutions.add(entry);
//		}
		return otherSolutions;
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
		logger.info("Question: " + question);
		this.currentQuestion = question;
		Answer answer = null;
		try {
			uri2Item = new HashMap<String, BasicResultItem>();
			property2URI2Values = new HashMap<String, Map<String,Set<Object>>>();
			message("Running...");
			learnedSPARQLQuery = null;
//			tbsl.setQuestion(question);
//			tbsl.learnSPARQLQueries();
			TemplateInstantiation ti = activeTBSL.getTBSL().answerQuestion(question);
			learnedSPARQLQuery = ti.getQuery();
					
			if(learnedSPARQLQuery != null){
				logger.info("Found answer.");
				logger.info("Learned SPARQL Query:\n" + learnedSPARQLQuery);
				answer = createAnswer(learnedSPARQLQuery);
				if(progressListener != null){
					progressListener.foundAnswer(true);
				}
			} else {
				logger.info("Found no answer.");
				message("Could not find an non-empty answer. Using fallback by searching in descriptions of the entities.");
				answer = answerQuestionFallback(question);
			}
		} catch (NoTemplateFoundException e) {
			logger.error("Found no template.");
			message("Didn't understand the question. Using fallback by searching in descriptions of the entities.");
			answer = answerQuestionFallback(question);
			if(progressListener != null){
				progressListener.foundAnswer(false);
			}
		}
//		message("Finished.");
		if(progressListener != null){
			progressListener.finished(answer);
		}
		
		return answer;
	}
	
	public Answer createAnswer(String sparqlQueryString){
		learnedSPARQLQuery = sparqlQueryString;
		Answer answer = null;
		String translatedQuery = getNLRepresentation(sparqlQueryString);
		message("Found answer for \"" + translatedQuery + "\". Loading result...");
		Query q = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ);
		if(!q.hasGroupBy()){
			q.setDistinct(true);
			learnedSPARQLQuery = q.toString();
		}
		
		
		if(q.isSelectType()){
			
			List<BasicResultItem> result = fetchResult(sparqlQueryString);
			Map<String, Integer> additionalProperties = getAdditionalProperties();
			
			List<String> mostProminentProperties = new ArrayList<String>();
			if(!result.isEmpty()){
				List<Entry<String, Integer>> sortedByValues = sortByValues(additionalProperties);
				for(int i = 0; i < Math.min(sortedByValues.size(), 5); i++){
					String propertyURI = sortedByValues.get(i).getKey();
					mostProminentProperties.add(propertyURI);
					additionalProperties.remove(propertyURI);
				}
				fillItems(mostProminentProperties);
			}
			
			answer = new SelectAnswer(result, mostProminentProperties, additionalProperties);
			if(result.isEmpty()){
				message("Answer for \"" + translatedQuery + "\" is empty.");
			} else {
				message("Found answer for \"" + translatedQuery + "\".");
			}
		} else if(q.isAskType()){
			
		}
		return answer;
	}
	
	private List<BasicResultItem> fetchResult(String query){
		Query extendedSPARQLQuery = extendSPARQLQuery(learnedSPARQLQuery);
		logger.info("Loading result...");
//		message("Loading result");
		List<BasicResultItem> result = new ArrayList<BasicResultItem>();
		ResultSet rs = executeSelect(extendedSPARQLQuery.toString());
		QuerySolution qs;
		String targetVar = getActiveTBSL().getTargetVar();
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
			Map<String, String> optionalProperties = getActiveTBSL().getOptionalProperties();
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
			if(getActiveTBSL().getInfoBoxClass() == OxfordInfoLabel.class){
				Double price = null;
				Literal priceLit = qs.getLiteral("price");
				try {
					if(priceLit != null){
						price = priceLit.getDouble();
					}
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
		if(getActiveTBSL().getInfoBoxClass() == OxfordInfoLabel.class){
			Map<String, Set<Object>> uri2Values = new HashMap<String, Set<Object>>();
			for(BasicResultItem item : result){
				String uri = item.getUri();
				Double price = (Double) item.getData().get("price");
				if(price != null){
					uri2Values.put(uri, Collections.<Object>singleton(price));
				}
			}
			property2URI2Values.put("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice", uri2Values);
		}
		logger.info("...done.");
		return result;
	}
	
	
	public String getAnswerAsSPARQLQuery(String question){
		learnedSPARQLQuery = null;		
//		tbsl.setQuestion(question);
		try {
//			tbsl.learnSPARQLQueries();
//			learnedSPARQLQuery = tbsl.getBestSPARQLQuery();
			learnedSPARQLQuery=activeTBSL.getTBSL().answerQuestion(question).getQuery();
		} catch (NoTemplateFoundException e) {
			e.printStackTrace();
		}
		return learnedSPARQLQuery;
	}
	
	private Answer answerQuestionFallback(String question){
		logger.info("Using fallback.");
		
		List<BasicResultItem> result = fallback.getData(question, 100, 0);
		//hack if OXford KB we add the price relation, because we need this for the price chart view
		if(activeTBSL==ExtendedTBSL.OXFORD){
			Map<String, Set<Object>> uri2Values = new HashMap<String, Set<Object>>();
			String uri;
			Object price;
			for(BasicResultItem item : result){
				uri = item.getUri();
				price = item.getData().get("price");
				if(uri != null && price != null){
					uri2Values.put(uri, Collections.singleton(price));
				}
			}
			property2URI2Values.put("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice", uri2Values);
		}
		
		
		Map<String, Integer> additionalProperties = getAdditionalProperties();
		
		return new SelectAnswer(result, Collections.<String>emptyList(), additionalProperties);
	}
	
	public boolean isDataProperty(String propertyURI){
		return dataProperties.contains(propertyURI);
	}
	
	public Map<String, Integer> getAdditionalProperties(){
		Map<String, Integer> properties = new HashMap<String, Integer>();
		if(getActiveTBSL().isAllowAdditionalProperties() && learnedSPARQLQuery !=null){
			logger.info("Loading additional,common properties...");
			Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
			ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
			ElementPathBlock pb = null;
			for(Element el : wherePart.getElements()){
				if(el instanceof ElementPathBlock){
					pb = (ElementPathBlock) el;
					break;
				}
			}
			
			String targetVar = getActiveTBSL().getTargetVar();
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
				if(!getActiveTBSL().getPropertyBlackList().contains(propertyURI)){
					properties.put(propertyURI, cnt);
				}
			}
		}
		logger.info("...done.");
		return properties;
	}
	
	public void fillItems(String propertyURI){
		logger.info("Filling data with " + propertyURI + "...");
		Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
		ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
		ElementPathBlock pb = null;
		for(Element el : wherePart.getElements()){
			if(el instanceof ElementPathBlock){
				pb = (ElementPathBlock) el;
				break;
			}
		}
		String targetVar = getActiveTBSL().getTargetVar();
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
		logger.info("...done.");
	}
	
	public void fillItems(List<String> propertyURIs){
		if(propertyURIs.isEmpty()){
			return;
		}
		logger.info("Filling data with " + propertyURIs + "...");
		Query extendedSPARQLQuery = QueryFactory.create(learnedSPARQLQuery, Syntax.syntaxARQ);
		ElementGroup wherePart = (ElementGroup)extendedSPARQLQuery.getQueryPattern();
		ElementPathBlock pb = null;
		for(Element el : wherePart.getElements()){
			if(el instanceof ElementPathBlock){
				pb = (ElementPathBlock) el;
				break;
			}
		}
		String targetVar = getActiveTBSL().getTargetVar();
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
		logger.info("...done.");
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
		if(property2URI2Values.containsKey(propertyURI)){
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
		
		String targetVar = getActiveTBSL().getTargetVar();
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
		Triple triple = new Triple(Node.createVariable(targetVar), Node.createURI(getActiveTBSL().getLabelPropertyURI()), Node.createVariable("label"));
		ElementFilter filter = null;
		String lang = getActiveTBSL().getLabelPropertyLanguage();
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
		triple = new Triple(Node.createVariable(targetVar), Node.createURI(getActiveTBSL().getDescriptionPropertyURI()), Node.createVariable("desc"));
		filter = null;
		lang = getActiveTBSL().getLabelPropertyLanguage();
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
		if(getActiveTBSL().getImagePropertyURI() != null){
			Triple imgTriple = new Triple(Node.createVariable(targetVar), Node.createURI(getActiveTBSL().getImagePropertyURI()), Node.createVariable("img"));
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
		if(getActiveTBSL().getInfoBoxClass() == OxfordInfoLabel.class){
			ElementGroup eg = new ElementGroup();
			ElementOptional optionalEl = new ElementOptional(eg);
			eg.addTriplePattern(new Triple(Node.createVariable("offer"), Node.createURI("http://purl.org/goodrelations/v1#includes"), Node.createVariable(targetVar)));
			eg.addTriplePattern(new Triple(Node.createVariable("offer"), Node.createURI("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice"), Node.createVariable("price")));
			vars.add("price");
			wherePart.addElement(optionalEl);
		}
		Map<String, String> mandatoryProperties = getActiveTBSL().getMandatoryProperties();
		if(mandatoryProperties != null){
			
		}
		Map<String, String> optionalProperties = getActiveTBSL().getOptionalProperties();
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
		logger.info("Executing SPARQL query\n" + sparqlQuery);
		ResultSet rs = null;
		try {
			Knowledgebase kb = getActiveTBSL().getTBSL().getKnowledgebase();
			if(kb instanceof RemoteKnowledgebase){
				SparqlEndpoint endpoint = ((RemoteKnowledgebase) kb).getEndpoint();
				if (cache == null) {
					QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), sparqlQuery);
					qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
					rs = qe.execSelect();
				} else {
					rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, sparqlQuery));
				}
			} else {
				rs = QueryExecutionFactory.create(sparqlQuery, ((LocalKnowledgebase) kb).getModel()).execSelect();
			}
			
			logger.info("...done.");
		} catch (Exception e) {
			logger.error("Error executing SPARQL query.", e);
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
	
	public static Query normalizeVarNames(Query query){
		Query copy = QueryFactory.create(query);
		
		
		List<String> candidates = new LinkedList<String>(Arrays.asList(new String[]{"x", "y", "z"}));
		//find all vars of form ?LetterNumber
		SortedSet<String> vars = new TreeSet<String>();
		String queryString = query.toString();
		System.out.println(queryString);
		String regex = "\\?[A-Za-z][0-9]+\\s";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(queryString);
		while (matcher.find()) {
			vars.add(matcher.group().trim());
		}
		System.out.println(vars);
		int cnt = 0;
		for(String var : vars){
			String replacement;
			if(!candidates.isEmpty()){
				replacement = candidates.remove(0);
			} else {
				replacement = "x" + cnt++;
			}
			queryString = queryString.replace(var, "?" + replacement);
		}
		System.out.println(queryString);
		
		
		return copy;
	}
	
	private String normalizeVarNames(String query){
		
		
		List<String> candidates = new LinkedList<String>(Arrays.asList(new String[]{"x", "y", "z"}));
		//find all vars of form ?LetterNumber
		SortedSet<String> vars = new TreeSet<String>();
		String regex = "\\?[A-Za-z][0-9]+\\s";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(query);
		while (matcher.find()) {
			vars.add(matcher.group().trim());
		}
		System.out.println(vars);
		int cnt = 0;
		for(String var : vars){
			String replacement;
			if(!candidates.isEmpty()){
				replacement = candidates.remove(0);
			} else {
				replacement = "x" + cnt++;
			}
			query = query.replace(var, "?" + replacement);
		}
		System.out.println(query);
		
		
		return query;
	}

	public ExtendedTBSL getActiveTBSL()
	{
		return activeTBSL;
	}

	public void setActiveTBSL(ExtendedTBSL eTBSL)
	{
		this.activeTBSL = eTBSL;
	}
	
}