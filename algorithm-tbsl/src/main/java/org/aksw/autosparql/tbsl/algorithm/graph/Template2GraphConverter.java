package org.aksw.autosparql.tbsl.algorithm.graph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.Template;
import org.aksw.autosparql.tbsl.algorithm.util.TriplePatternExtractor;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.vocabulary.RDF;

public class Template2GraphConverter {

	public TemplateGraph convert(Template template){
		Query query = QueryFactory.create(template.getQuery().toString());
		List<Slot> slots = template.getSlots();
		Map<String, Slot> var2Slot = new HashMap<String, Slot>();
		for (Slot slot : slots) {
			var2Slot.put(slot.getAnchor(), slot);
		}
		
		TriplePatternExtractor extractor = new TriplePatternExtractor();
		Set<Triple> triples = extractor.extractTriplePattern(query);
		
		//process rdf:type triples, i.e. remove them from the triples because we merge their information into the other nodes
		Map<String, Slot> var2ClassSlot = new HashMap<String, Slot>();
		for (Iterator<Triple> iterator = triples.iterator(); iterator.hasNext();) {
			Triple triple = iterator.next();
			if(triple.predicateMatches(RDF.type.asNode())){
				String subjectVarName = triple.getSubject().getName();
				String objectVarName = triple.getObject().getName();
				var2ClassSlot.put(subjectVarName, var2Slot.get(objectVarName));
				iterator.remove();
			}
		}
		//build the graph
		TemplateGraph graph = new TemplateGraph();
		//create the node for the projection variable
		String projectionVar = query.getProjectVars().get(0).getVarName();
		TemplateNode rootNode = new TemplateNode(projectionVar);
		graph.addVertex(rootNode);
		TemplateNode sourceNode;
		TemplateNode targetNode;
		for (Triple triple : triples) {
			com.hp.hpl.jena.graph.Node subject = triple.getSubject();
			com.hp.hpl.jena.graph.Node predicate = triple.getPredicate();
			com.hp.hpl.jena.graph.Node object = triple.getObject();
			if(subject.getName().equals(projectionVar)){
				sourceNode = rootNode;
			} else {
				sourceNode = new TemplateNode(var2Slot.get(subject.getName()));
			}
			if(object.getName().equals(projectionVar)){
				targetNode = rootNode;
			} else {
				targetNode = new TemplateNode(var2Slot.get(object.getName()));
			}
			
			//add type informations to nodes
			for (Entry<String, Slot> entry : var2ClassSlot.entrySet()) {
				String var = entry.getKey();
				Slot slot = entry.getValue();
				if(var.equals(subject.getName())){
					sourceNode.addType(slot);
				} 
				if(var.equals(object.getName())){
					targetNode.addType(slot);
				}
			}
			
			//process predicate
			Slot predicateSlot = var2Slot.get(predicate.getName());
			TemplateEdge edge = new TemplateEdge(predicateSlot);
			
			graph.addVertex(sourceNode);
			graph.addVertex(targetNode);
			graph.addEdge(sourceNode, targetNode, edge);
			
		}
		return graph;
	}

}
