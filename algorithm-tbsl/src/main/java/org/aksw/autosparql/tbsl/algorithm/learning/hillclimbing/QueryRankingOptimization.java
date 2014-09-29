/**
 * 
 */
package org.aksw.autosparql.tbsl.algorithm.learning.hillclimbing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.aksw.autosparql.commons.qald.EvaluationUtils;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.tbsl.algorithm.learning.NoTemplateFoundException;
import org.aksw.autosparql.tbsl.algorithm.learning.TBSL;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslDbpedia;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;

/**
 * @author gerb
 *
 */
public class QueryRankingOptimization {
	
	
	private static final Logger logger = Logger.getLogger(QueryRankingOptimization.class.getName());

	private static final int NUMBER_OF_RANDOM_INITIALIZATIONS	= 10;
	private static final int NUMBER_OF_FEATURES					= 5;
	private static final Double STEP_SIZE						= 0.1;
	private static final String LANGUAGE						= "en";
	private static TBSL tbsl = TbslDbpedia.INSTANCE;
	private static List<Question> questions;
	private static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	private static ExtractionDBCache cache = new ExtractionDBCache("/opt/tbsl/cache");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		init();
		
		System.out.println("Total f-Measure: " + getFMeasure(generateParameters(NUMBER_OF_FEATURES)));
		System.exit(0);
		
		// define global maximum f measure and parameters
		Double globalMaxFMeasure = 0D;
		List<Double> globalMaxSolution = new ArrayList<Double>();
		
		// define number of random initializations
		for ( int i = 0, steps = 0 ; i < NUMBER_OF_RANDOM_INITIALIZATIONS; i++, steps = 0) {
		
			List<Double> currentSolution  = generateParameters(NUMBER_OF_FEATURES);
			Double currentFScore		  = getFMeasure(currentSolution);

			while (true) {
	        	
	        	// try to replace each single component w/ its neighbors
	            Double highestFScore			= currentFScore;
	            List<Double> highestSolution	= currentSolution;
	            
	            // test all neighbours if they achieve higher f measure
	            for (List<Double> newSolution : generateNeighbors(currentSolution)) {
	            	
	            	Double neighbourCost = getFMeasure(newSolution);
	                if ( neighbourCost > highestFScore ) {

	                	highestFScore = neighbourCost;
	                    highestSolution = newSolution;
	                }
	            }
	            
	            // neighbors did not return better results, stop hill climbing
	            if ( highestFScore <= currentFScore ) break;
	            else {
	            	// get to next step since neighbors achieved higher f measure
	            	currentSolution = highestSolution;
	    			currentFScore = highestFScore;
	                steps++;
	            }
	            
	            System.out.println(String.format("F1-Score at step %s: %s", steps, highestFScore));
	        }
	        
			// this random initialization was better then the previous ones 
	        if ( currentFScore > globalMaxFMeasure ) {
	        	
	        	globalMaxFMeasure = currentFScore;
	        	globalMaxSolution = currentSolution;
	        }
		}
		
		System.out.println("Best overall F-Measure: " + globalMaxFMeasure);
		System.out.println("Parameters: " + globalMaxSolution);
	}

	/**
	 * 
	 */
	private static void init() {
		
//		SparqlEndpoint endpoint		= SparqlEndpoint.getEndpointDBpedia();
//		// solr resources index
//		SOLRIndex resourcesIndex	= new SOLRIndex("http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_resources");
//		resourcesIndex.setPrimarySearchField("label");
//		
//		// solr classes, properties and boa index
//		Index classesIndex				= new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_classes");
//		Index propertiesIndex			= new SOLRIndex("http://139.18.2.173:8080/solr/dbpedia_properties");
//		SOLRIndex boa_propertiesIndex	= new SOLRIndex("http://139.18.2.173:8080/solr/boa_fact_detail");
//		boa_propertiesIndex.setSortField("boa-score");
//		propertiesIndex = new HierarchicalIndex(boa_propertiesIndex, propertiesIndex);
		
//		Knowledgebase kb = new RemoteKnowledgebase(endpoint, null, null, resourcesIndex, propertiesIndex, classesIndex, null);
//		tbsl = new TBSL(kb);
		
		questions = QaldLoader.loadAndSerializeQuestions(Arrays.asList("en","de"), 
				"de_wac_175m_600.crf.ser.gz", "english.conll.4class.distsim.crf.ser.gz",
				"german-dewac.tagger", "english-left3words-distsim.tagger", false);
	}

	/**
	 * 
	 * @param numberOfFeatures
	 * @return
	 */
	private static List<Double> generateParameters(int numberOfFeatures) {
		
		List<Double> randomNumbers = new ArrayList<Double>();
	    Random random = new Random();
	    
	    while ( randomNumbers.size() < numberOfFeatures ) 
            randomNumbers.add(random.nextDouble());
		
		return randomNumbers;
	}

	/**
	 * 
	 * @param currentSolution
	 * @return
	 */
	private static List<List<Double>> generateNeighbors(List<Double> currentSolution) {
		
		double upperBound = 1D;
    	double lowerBound = 0D;
    	
    	List<List<Double>> neighbours = new ArrayList<List<Double>>();
    	
    	for (int i = 0; i < currentSolution.size(); i++){
    		
			List<Double> newNeighbour = new ArrayList<Double>(currentSolution);
            if ( newNeighbour.get(i) < upperBound ) {
            	
            	double newParamterValue = newNeighbour.get(i) + STEP_SIZE ;
            	newNeighbour.set(i, newParamterValue < upperBound ? newParamterValue : 1);
            	neighbours.add(newNeighbour);
            }
            newNeighbour = new ArrayList<Double>(currentSolution);
            if ( newNeighbour.get(i) > lowerBound ) {
            	
            	double newParamterValue = newNeighbour.get(i) - STEP_SIZE;
            	newNeighbour.set(i, newParamterValue > lowerBound ? newParamterValue : 0);
            	neighbours.add(newNeighbour);
            }
    	}
    	return neighbours;
	}

	private static Double getFMeasure(List<Double> parameters) {
		
		double totalFMeasure = 0D;
		
		for ( Question q : questions ) {
			TemplateInstantiation templateInstance = null;
			try {
				
				templateInstance = tbsl.answerQuestion(q.languageToQuestion.get(LANGUAGE), parameters);
				
				String sparqlQuery = templateInstance.asQuery().toString();
				String targetSPARQLQuery = q.sparqlQuery;
				int id = q.id;
				double precision = EvaluationUtils.precision(sparqlQuery, targetSPARQLQuery, endpoint, cache);
				double recall	 = EvaluationUtils.recall(sparqlQuery, targetSPARQLQuery, endpoint, cache);
				double fMeasure	 = EvaluationUtils.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint, cache);
				// sum up the f measure for each question, since the run with 
				// the highest sum of all f-scores is the best configuration
				totalFMeasure += fMeasure;

				System.out.println(q.id + ": " + q.languageToQuestion.get(LANGUAGE));
				System.out.println("Precision: " + precision + "\tRecall: " + recall + " F1: " + fMeasure +"\n");
				logger.info(q.id + ": " + q.languageToQuestion.get(LANGUAGE));
				logger.info("Precision: " + precision + "\tRecall: " + recall + " F1: " + fMeasure +"\n");
				logger.info(sparqlQuery);
			} 
			catch (NoTemplateFoundException e) {
				// no template means: f-measure of 0,
				// so we don't need to add anything to the overall f-measure
				logger.error("No template generated for " + q.languageToQuestion.get(LANGUAGE), e);
			} catch (com.hp.hpl.jena.query.QueryParseException e) {
				logger.warn(
						"Invalid SPARQL query\n" + templateInstance==null?null:templateInstance.getQuery() + " for "
								+ q.languageToQuestion.get(LANGUAGE), e);
			}
			catch (Exception e) {
				logger.error("Error occurred for " + q.languageToQuestion.get(LANGUAGE), e);
			}
		}
		return totalFMeasure;
	}
}
