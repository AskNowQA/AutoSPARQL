package org.aksw.autosparql.tbsl.algorithm;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.aksw.autosparql.commons.index.Indices;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.DBpediaKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.OxfordKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.tbsl.algorithm.learning.Entity;
import org.aksw.autosparql.tbsl.algorithm.learning.TBSL;
import org.aksw.autosparql.tbsl.algorithm.learning.TemplateInstantiation;
import org.aksw.autosparql.tbsl.algorithm.learning.ranking.SimpleRankingComputation;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.util.Prominences;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TBSLTest extends TestCase{
	
//	private static final String	SOLR_SERVER_URI_EN	= "http://http://solr.aksw.org/solr/en_";
//	static Model model;
//	static
//	{
//		model = ModelFactory.createMemModelMaker().createDefaultModel();
//		model.read(OxfordKnowledgebase.class.getClassLoader().getResourceAsStream("oxford.ttl"),null,"TTL");
//	}

//	@Override
//	protected void setUp() throws Exception {		
//		super.setUp();
//		
////		endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
//		//		model = ModelFactory.createOntologyModel();
//		//		File dir = new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/data");
//		//		try {
//		//			for(File f : dir.listFiles()){
//		//				if(f.isFile()){
//		//					System.out.println("Loading file " + f.getName());
//		//					try {
//		//						model.read(new FileInputStream(f), null, "TURTLE");
//		//					} catch (Exception e) {
//		//						System.err.println("Parsing failed.");
//		//						e.printStackTrace();
//		//					}
//		//				}
//		//			}
//		//			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/ontology.ttl")), null, "TURTLE");
//		//		} catch (FileNotFoundException e) {
//		//			e.printStackTrace();
//		//		}
////		assertNotNull(endpoint);
////		assertNotNull(model);
//	}

	@Test
	public void testDBpedia() throws Exception{
		//		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
		//		
		TBSL learner = new TBSL(DBpediaKnowledgebase.INSTANCE);
		learner.init();

		String question = "Give me all books written by Dan Brown";

		TemplateInstantiation ti = learner.answerQuestion(question);
		System.out.println(ti.getQuery());
		//		learner.learnSPARQLQueries();
		//		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
		//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		//		System.out.println(learner.getLearnedPosition());
	}

//	//	@Test
//	public void testOxfordIndices()
//	{
//		System.out.println(classIndex.getResources("House"));
//		System.out.println(classIndex.getResources("house"));
//		System.out.println(classIndex.getResources("houses"));
//	}

	@Test
	public void testProminence()
	{
		Entity bedrooms = new Entity("http://diadem.cs.ox.ac.uk/ontologies/real-estate#bedrooms", "number of bedrooms");		
		Slot slot = new Slot("p1",SlotType.DATATYPEPROPERTY,Arrays.asList(new String[]{"BEDROOMS"}));		
		HashMap<Slot, Collection<Entity>> map = new HashMap<>();
		map.put(slot, Collections.singleton(bedrooms));
		Map<Slot, Prominences> scores = new SimpleRankingComputation(OxfordKnowledgebase.INSTANCE).computeEntityProminenceScoresWithReasoner(map);
		assertTrue(scores.values().iterator().next().values().iterator().next()>800);
	}

		@Test
		public void testOxfordLocal() throws Exception
		{
		//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(model, resourcesIndex, classesIndex, propertiesIndex);
		TBSL learner = new TBSL(OxfordKnowledgebase.INSTANCE);
		learner.init();
		String question = "Give me all houses with more than 3 bedrooms.";// and more than 2 bedrooms
		TemplateInstantiation ti = learner.answerQuestion(question);

		System.out.println("Learned query:\n" + ti.getQuery());
		//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
		//		System.out.println(learner.getLearnedPosition());
		}

	//	@Test
	//	public void testOxfordRemote() throws Exception{
	//		ExtractionDBCache cache = new ExtractionDBCache("cache");
	//		assertNotNull(cache);
	//		SPARQLIndex resourcesIndex = new VirtuosoResourcesIndex(endpoint, cache);
	//		SPARQLIndex classesIndex = new VirtuosoClassesIndex(endpoint, cache);
	//		SPARQLIndex propertiesIndex = new VirtuosoPropertiesIndex(endpoint, cache);
	//		MappingBasedIndex mappingIndex= new MappingBasedIndex(
	//				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_class_mappings.txt").getPath(), 
	//				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_resource_mappings.txt").getPath(),
	//				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_dataproperty_mappings.txt").getPath(),
	//				OxfordEvaluation.class.getClassLoader().getResource("tbsl/oxford_objectproperty_mappings.txt").getPath()
	//				);
	//		
	//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(endpoint, resourcesIndex, classesIndex, propertiesIndex);
	//		learner.setMappingIndex(mappingIndex);
	//		learner.init();
	//		learner.setGrammarFiles(new String[]{"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"});
	//		
	//		String question = "Give me all houses near a school.";
	//		question = "Give me all houses with more than 3 bathrooms and more than 2 bedrooms.";
	//		question = "Give me all Victorian houses in Oxfordshire";
	//		question = "Edwardian houses close to supermarket for less than 1,000,000 in Oxfordshire";
	////		question = "Give me all family houses with more than 2 bathrooms and more than 4 bedrooms";
	//		
	//		learner.setQuestion(question);
	//		learner.learnSPARQLQueries();
	//		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
	//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
	//		System.out.println(learner.getLearnedPosition());
	//	}

//	@Test
//	public void testSPARQLIndex(){
//		Index classesIndex = new SPARQLClassesIndex(model);
//		System.out.println(classesIndex.getResources("flat"));
//	}

//	@Test
//	public void testSPARQLPropertyPathNegation(){
//		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
//				"PREFIX gr: <http://purl.org/goodrelations/v1#> " +
//				"PREFIX ex: <http://diadem.cs.ox.ac.uk/ontologies/real-estate#>" +
//				"SELECT * WHERE {?s a gr:Offering. ?s (!rdfs:label)+ ?o. ?o a ex:RoomSpecification.?o ?p ?o1} LIMIT 50";
//		System.out.println(QueryFactory.create(query, Syntax.syntaxARQ));
//		ResultSet rs  =QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();
//		while(rs.hasNext()){
//			System.out.println(rs.next());
//		}
//	}
}