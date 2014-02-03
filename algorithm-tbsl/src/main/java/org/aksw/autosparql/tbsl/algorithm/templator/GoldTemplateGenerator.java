package org.aksw.autosparql.tbsl.algorithm.templator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.tbsl.algorithm.sparql.GoldTemplate;
import org.aksw.autosparql.tbsl.algorithm.sparql.Slot;
import org.aksw.autosparql.tbsl.algorithm.sparql.SlotType;
import org.aksw.autosparql.tbsl.algorithm.util.TriplePatternExtractor;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.vocabulary.RDF;


public class GoldTemplateGenerator {

	public GoldTemplateGenerator() {
		SPARQLReasoner reasoner = new SPARQLReasoner(new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia()));
		List<Question> questions = QaldLoader.loadAndSerializeQuestions(Arrays.asList("en"), "de_wac_175m_600.crf.ser.gz", "english.all.3class.distsim.crf.ser.gz",
				"german-dewac.tagger", "english-left3words-distsim.tagger", false);
		
		TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
		Map<Integer, GoldTemplate> id2Template = new HashMap<Integer, GoldTemplate>();
		for (Question question : questions) {
			if(question.outOfScope) continue;//if(question.id != 85) continue;
			
			String sparqlQueryString = question.sparqlQuery;
			//extract URIs and replace them with variables
			Map<String, String> var2URI = new HashMap<String, String>();
			Pattern pattern = Pattern.compile("\\b\\w+:(\\w|\\.)+\\b");
			Matcher matcher = pattern.matcher(sparqlQueryString);
			int i = 0;
//			System.out.println(sparqlQueryString);
			Set<String> replacedURIs = new HashSet<String>();
			while (matcher.find()) {
				String uri = matcher.group();
				if(!uri.equals("rdf:type") && !uri.startsWith("xsd") && !replacedURIs.contains(uri)){
					String var = "var" + i++;
					sparqlQueryString = sparqlQueryString.replaceAll("\\b" + uri + "\\b", "?" + var);
					var2URI.put(var, uri);
					replacedURIs.add(uri);
				}
			}
			Query sparqlQuery = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ);
//			System.out.println(sparqlQuery);
			GoldTemplate goldTemplate = new GoldTemplate(sparqlQuery);
			//create the slots
			Set<Triple> triplePattern = triplePatternExtractor.extractTriplePattern(sparqlQuery);
			Set<String> processedVariables = new HashSet<String>();
			for (Triple triple : triplePattern) {
				//CLASS slot for rdf:type statements
				if(triple.getPredicate().equals(RDF.type.asNode())){
					//add RESOURCE slot if subject is URI
					Node subject = triple.getSubject();
					String var = subject.getName();
					String uri = var2URI.get(var);
					if(uri != null && !processedVariables.contains(var)){
						String preToken = uri.substring(uri.indexOf(':')+1, uri.length());
						Slot slot = new Slot(var, SlotType.RESOURCE, Collections.singletonList(preToken));
						goldTemplate.addSlot(slot);
						processedVariables.add(var);
					}
					//add CLASS slot if subject is URI
					Node object = triple.getObject();
					var = object.getName();
					uri = var2URI.get(var);
					if(uri != null){
						String preToken = uri.substring(uri.indexOf(':')+1, uri.length());
						Slot slot = new Slot(var, SlotType.CLASS, Collections.singletonList(preToken));
						goldTemplate.addSlot(slot);
						var2URI.remove(var);
					}
					
				} else if(triple.getPredicate().isVariable()){
					//slot for subject
					Node subject = triple.getSubject();
					String var = subject.getName();
					String uri = var2URI.get(var);
					if(uri != null){
						String preToken = uri.substring(uri.indexOf(':')+1, uri.length());
						Slot slot = new Slot(var, SlotType.RESOURCE, Collections.singletonList(preToken));
						goldTemplate.addSlot(slot);
						var2URI.remove(var);
					}
					//slot for predicate
					var = triple.getPredicate().getName();
					uri = var2URI.get(var);
					String preToken = uri.substring(uri.indexOf(':')+1, uri.length());
					uri = sparqlQuery.expandPrefixedName(uri);
					boolean dataProperty = reasoner.isDataProperty(uri, true);
					Slot slot = null;
					if(dataProperty){
						slot = new Slot(var, SlotType.DATATYPEPROPERTY, Collections.singletonList(preToken));
					} else {
						boolean objectProperty = reasoner.isObjectProperty(uri, true);
						if(objectProperty){
							slot = new Slot(var, SlotType.OBJECTPROPERTY, Collections.singletonList(preToken));
							//slot for object
							Node object = triple.getObject();
							String objectVar = object.getName();
							String objectURI = var2URI.get(objectVar);
							if(objectURI != null){
								preToken = objectURI.substring(objectURI.indexOf(':')+1, objectURI.length());
								Slot objectSlot = new Slot(objectVar, SlotType.RESOURCE, Collections.singletonList(preToken));
								goldTemplate.addSlot(objectSlot);
							}
						} else {
							System.err.println(uri);
						}
					} 
					if(!processedVariables.contains(var)){
						goldTemplate.addSlot(slot);
						processedVariables.add(var);
					}
					
				} else {
					//should not happen
				}
			}
			id2Template.put(question.id, goldTemplate);
		}
		serialize(id2Template);
	}
	
	private void serialize(Map<Integer, GoldTemplate> questionID2Template){
		try {
			
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(QaldLoader.class.getResource("/qald/dbpedia-train.xml").getFile()));
            doc.getDocumentElement().normalize();
            NodeList questionNodes = doc.getElementsByTagName("question");
            
			for (int i = 0; i < questionNodes.getLength(); i++) {

				Element questionNode = (Element) questionNodes.item(i);

				int id = Integer.valueOf(questionNode.getAttribute("id"));
				GoldTemplate goldTemplate = questionID2Template.get(id);
				if(goldTemplate != null){
					Element templateNode = doc.createElement("template");
					questionNode.appendChild(templateNode);
					Element templateQueryNode = doc.createElement("query");
					templateNode.appendChild(templateQueryNode);
					templateQueryNode.appendChild(doc.createCDATASection(goldTemplate.getQuery().toString()));
					Element templateSlotsNode = doc.createElement("slots");
					templateNode.appendChild(templateSlotsNode);
					for (Slot slot : goldTemplate.getSlots()) {
						Element slotNode = doc.createElement("slot");
						Element anchorNode = doc.createElement("anchor");
						anchorNode.appendChild(doc.createCDATASection(slot.getAnchor()));
						slotNode.appendChild(anchorNode);
						Element slottypeNode = doc.createElement("type");
						slottypeNode.appendChild(doc.createCDATASection(slot.getSlotType().name()));
						slotNode.appendChild(slottypeNode);
						Element tokenNode = doc.createElement("token");
						tokenNode.appendChild(doc.createCDATASection(slot.getWords().get(0)));
						slotNode.appendChild(tokenNode);
						templateSlotsNode.appendChild(slotNode);
					}
				}
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			  Transformer transformer = transformerFactory.newTransformer();
			  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			  DOMSource source = new DOMSource(doc);
			  StreamResult streamResult =  new StreamResult(new File("dbpedia-train-with-templates.xml"));
			  transformer.transform(source, streamResult);
		} 
		catch (DOMException e) {
	            e.printStackTrace();
	    }
		catch (ParserConfigurationException e) {
	            e.printStackTrace();
	    }
		catch (SAXException e) {
	            e.printStackTrace();
	    } 
		catch (IOException e) {
	            e.printStackTrace();
	    } catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new GoldTemplateGenerator();
	}

}
