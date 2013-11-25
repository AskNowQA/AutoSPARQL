package org.aksw.autosparql.algorithm.tbsl;

import java.net.URL;
import java.util.Collections;
import junit.framework.TestCase;
import org.aksw.autosparql.algorithm.tbsl.learning.TBSL;
import org.aksw.autosparql.algorithm.tbsl.learning.TemplateInstantiation;
import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;
import org.aksw.autosparql.algorithm.tbsl.util.LocalKnowledgebase;
import org.aksw.autosparql.algorithm.tbsl.util.RemoteKnowledgebase;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.SOLRIndex;
import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.junit.Test;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TBSLTest extends TestCase{
	private static final String	SOLR_SERVER_URI_EN	= "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_";
	private Model model = null;
	private SparqlEndpoint endpoint;
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();
		model = ModelFactory.createMemModelMaker().createDefaultModel();
		model.read(this.getClass().getClassLoader().getResourceAsStream("oxford.ttl"),null,"TTL");
		endpoint = new SparqlEndpoint(new URL("http://lgd.aksw.org:8900/sparql"), Collections.singletonList("http://diadem.cs.ox.ac.uk"), Collections.<String>emptyList());
//		model = ModelFactory.createOntologyModel();
//		File dir = new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/data");
//		try {
//			for(File f : dir.listFiles()){
//				if(f.isFile()){
//					System.out.println("Loading file " + f.getName());
//					try {
//						model.read(new FileInputStream(f), null, "TURTLE");
//					} catch (Exception e) {
//						System.err.println("Parsing failed.");
//						e.printStackTrace();
//					}
//				}
//			}
//			model.read(new FileInputStream(new File("/home/lorenz/arbeit/papers/question-answering-iswc-2012/examples/ontology.ttl")), null, "TURTLE");
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
		assertNotNull(endpoint);
		assertNotNull(model);
	}
	
	@Test
	public void testDBpedia() throws Exception{
//		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://greententacle.techfak.uni-bielefeld.de:5171/sparql"), 
		SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://dbpedia.org/sparql"),
		Collections.<String>singletonList(""), Collections.<String>emptyList());
		
		SOLRIndex resourcesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");
		SOLRIndex classesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_classes");
		SOLRIndex objectPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		SOLRIndex dataPropertiesIndex = new SOLRIndex(SOLR_SERVER_URI_EN+"dbpedia_data_properties");
		for(SOLRIndex index: new SOLRIndex[] {resourcesIndex,classesIndex,objectPropertiesIndex,dataPropertiesIndex})
		{
			index.setPrimarySearchField("label");
		}
		Knowledgebase kb = new RemoteKnowledgebase(endpoint,"dbpedia","DBpedia",resourcesIndex, classesIndex, objectPropertiesIndex,dataPropertiesIndex,null);
//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(kb);
		TBSL learner = new TBSL(kb);
		learner.init();
		
		String question = "Give me all books written by Dan Brown";
		
		TemplateInstantiation ti = learner.answerQuestion(question);
		System.out.println(ti.getQuery());
//		learner.learnSPARQLQueries();
//		System.out.println("Learned query:\n" + learner.getBestSPARQLQuery());
//		System.out.println("Lexical answer type is: " + learner.getTemplates().iterator().next().getLexicalAnswerType());
//		System.out.println(learner.getLearnedPosition());
	}
	
	@Test
	public void testOxfordLocal() throws Exception
	{
		assertNotNull(model);
		Index resourceIndex = new SPARQLIndex(model);
		Index classIndex = new SPARQLClassesIndex(model);
		Index objectPropertyIndex = new SPARQLObjectPropertiesIndex(model);
		Index dataPropertyIndex = new SPARQLDatatypePropertiesIndex(model);
		Knowledgebase kb = new LocalKnowledgebase(model, "oxford", "oxford", resourceIndex, objectPropertyIndex, dataPropertyIndex, classIndex,null);
//		SPARQLTemplateBasedLearner2 learner = new SPARQLTemplateBasedLearner2(model, resourcesIndex, classesIndex, propertiesIndex);
		TBSL learner = new TBSL(kb);
		learner.init();
		
		String question = "Give me all houses with more than 3 bathrooms.";// and more than 2 bedrooms
		
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
	
	@Test
	public void testSPARQLIndex(){
		Index classesIndex = new SPARQLClassesIndex(model);
		System.out.println(classesIndex.getResources("flat"));
	}
	
	@Test
	public void testSPARQLPropertyPathNegation(){
		String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX gr: <http://purl.org/goodrelations/v1#> " +
				"PREFIX ex: <http://diadem.cs.ox.ac.uk/ontologies/real-estate#>" +
				"SELECT * WHERE {?s a gr:Offering. ?s (!rdfs:label)+ ?o. ?o a ex:RoomSpecification.?o ?p ?o1} LIMIT 50";
		System.out.println(QueryFactory.create(query, Syntax.syntaxARQ));
		ResultSet rs  =QueryExecutionFactory.create(QueryFactory.create(query, Syntax.syntaxARQ), model).execSelect();
		while(rs.hasNext()){
			System.out.println(rs.next());
		}
		
	}

}