package org.aksw.autosparql.algorithm.tbsl.learning.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.aksw.autosparql.algorithm.tbsl.learning.TemplateInstantiation;
import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.aksw.autosparql.commons.metric.SPARQLEndpointMetrics;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.vocabulary.RDF;

public class TripleProbabilityFeatureExtractor extends AbstractFeatureExtractor{

	private SPARQLEndpointMetrics metrics;

	public TripleProbabilityFeatureExtractor(Knowledgebase knowledgebase, SPARQLEndpointMetrics metrics) {
		super(knowledgebase);
		this.metrics = metrics;
	}

	@Override
	public double extractFeature(TemplateInstantiation templateInstantiation) {
		double totalScore = computeTripleScore(templateInstantiation);
		return totalScore;
	}
	
	@Override
	public Feature getFeature() {
		return Feature.TRIPLE_PROBABILITY;
	}

	private double computeTripleScore(TemplateInstantiation instantiation){
		//extract all triples
		Set<Triple> triples = new HashSet<Triple>();
		com.hp.hpl.jena.query.Query query = QueryFactory.create(instantiation.asQuery());
		ElementGroup eg = (ElementGroup) query.getQueryPattern();
		for(Element e : eg.getElements()){
			for(Iterator<TriplePath> iter = ((ElementPathBlock) e).patternElts(); iter.hasNext();){
				Triple t = iter.next().asTriple();
				triples.add(t);
			}
		}
		//extract types of variables if exist
		Map<Node, Node> variableToClass = new HashMap<Node, Node>();
		for(Iterator<Triple> iter = triples.iterator(); iter.hasNext();){
			Triple t = iter.next(); 
			if(t.predicateMatches(RDF.type.asNode()) && t.getSubject().isVariable() && t.getObject().isURI()){
				variableToClass.put(t.getSubject(), t.getObject());
				iter.remove();
			}
		}
		//condense triples, i.e. if a triple contains a variable then we replace it by a class if exist
		Set<Triple> subjectClassTriples = new HashSet<Triple>();
		Set<Triple> objectClassTriples = new HashSet<Triple>();
		for(Iterator<Triple> iter = triples.iterator(); iter.hasNext();){
			Triple t = iter.next(); 
			Node subject = t.getSubject();
			Node object = t.getObject();
			
			if(subject.isVariable()){
				Node subjectClassNode = variableToClass.get(subject);
				Triple newTriple = Triple.create( 
						(subjectClassNode != null) ? subjectClassNode : subject, 
						t.getPredicate(), 
						object);
				subjectClassTriples.add(newTriple);
			} else if(object.isURI()){
				Node objectClassNode = variableToClass.get(object);
				Triple newTriple = Triple.create( 
						subject, 
						t.getPredicate(), 
						(objectClassNode != null) ? objectClassNode : object);
				objectClassTriples.add(newTriple);
			}
			
		}
		double totalScore = 0;
		for(Triple t : subjectClassTriples){
			if(t.getSubject().isURI() && t.getPredicate().isURI() && t.getObject().isURI()){
				double goodness = metrics.getGoodness(
						new NamedClass(t.getSubject().getURI()), 
						new ObjectProperty(t.getPredicate().getURI()),
						new Individual(t.getObject().getURI()));
				System.out.println(t + ": " + goodness);
				totalScore += goodness;
			}
		}
		for(Triple t : objectClassTriples){
			if(t.getSubject().isURI() && t.getPredicate().isURI() && t.getObject().isURI()){
				double goodness = metrics.getGoodness(
						new Individual(t.getSubject().getURI()), 
						new ObjectProperty(t.getPredicate().getURI()),
						new NamedClass(t.getObject().getURI()));
				System.out.println(t + ": " + goodness);
				totalScore += goodness;
			}
		}
		return totalScore;
	}
	

}
