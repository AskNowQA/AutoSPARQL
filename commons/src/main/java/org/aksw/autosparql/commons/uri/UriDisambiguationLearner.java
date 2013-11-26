/**
 * 
 */
package org.aksw.autosparql.commons.uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.autosparql.commons.qald.uri.GoldEntity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;

/**
 * @author gerb
 * 
 */
public class UriDisambiguationLearner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		List<Question> questions = QaldLoader.loadAndSerializeQuestions(Arrays.asList("en","de"), "de_wac_175m_600.crf.ser.gz", "english.conll.4class.distsim.crf.ser.gz",
				"german-dewac.tagger", "english-left3words-distsim.tagger", false);
		
		run(questions, "de");
		run(questions, "en");
	}
	
	private static void run(List<Question> questions, String language) {
		
		double maxScore = 0D;
		List<String> maxWrongResources = new ArrayList<String>();
		
//		for ( UriDisambiguation.APRIORI_PARAMETER = 0D ; UriDisambiguation.APRIORI_PARAMETER <= 1 ; UriDisambiguation.APRIORI_PARAMETER += 0.01) {
//			System.out.println("STEP " + UriDisambiguation.APRIORI_PARAMETER + " " + UriDisambiguation.STRING_SIMILARTY_PARAMETER + " " + UriDisambiguation.BONUS);
//				for ( UriDisambiguation.STRING_SIMILARTY_PARAMETER = 0D ; UriDisambiguation.STRING_SIMILARTY_PARAMETER <= 1 ; UriDisambiguation.STRING_SIMILARTY_PARAMETER += 0.01) {
//					for ( UriDisambiguation.BONUS = 1 ; UriDisambiguation.BONUS <= 10 ; UriDisambiguation.BONUS++) {
					
						int correctEnEntities = 0;
						int goldEntities = 0;
						List<String> wrongResources = new ArrayList<String>();
						
						for (Question q : questions) {
							
							for ( GoldEntity goldEntity : q.goldEntites.get(language) ) {
								
								boolean found = false;
								List<Resource> candidates = UriDisambiguation.getUriCandidates(goldEntity.label, language);
								
								for (Resource res : UriDisambiguation.getTopUris(
										candidates , goldEntity.label, language)) {

									if ( goldEntity.uri.equals(res.uri) ) {
										
										correctEnEntities++;
										found = true;
									}
								}
								if ( !found ) wrongResources.add("Question-" + q.id + ": " + goldEntity.label);
							}
							goldEntities += q.goldEntites.get(language).size();
						}
						
						double score = correctEnEntities / (double) goldEntities;
						System.out.println("Correct: " + correctEnEntities + " Gold: " + goldEntities + " Score: " +score);
						
						if ( score > maxScore ) {
							
							maxScore = score;
							maxWrongResources = wrongResources;
						}
//					}	
//				}
//		}
		for ( String res : maxWrongResources ) System.out.println(res);
	}
}
