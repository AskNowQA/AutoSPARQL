package org.aksw.autosparql.algorithm.tbsl.learning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.autosparql.algorithm.tbsl.sparql.Slot;
import org.aksw.autosparql.algorithm.tbsl.sparql.SlotType;
import org.aksw.autosparql.algorithm.tbsl.sparql.Template;
import org.aksw.autosparql.algorithm.tbsl.util.Knowledgebase;
import org.apache.log4j.Logger;
import org.dllearner.common.index.HierarchicalIndex;
import org.dllearner.common.index.Index;
import org.dllearner.common.index.IndexResultItem;
import org.dllearner.common.index.IndexResultSet;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import org.dllearner.common.index.SPARQLPropertiesIndex;
import org.dllearner.common.index.VirtuosoDatatypePropertiesIndex;
import org.dllearner.common.index.VirtuosoObjectPropertiesIndex;
import org.dllearner.common.index.VirtuosoPropertiesIndex;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;
import org.aksw.autosparql.commons.uri.Resource;
import org.aksw.autosparql.commons.uri.UriDisambiguation;

public class SimpleEntityDisambiguation {
	
	private static final Logger logger = Logger.getLogger(SimpleEntityDisambiguation.class.getName());
	
	private Knowledgebase knowledgebase;
	private SimpleIRIShortFormProvider iriSfp = new SimpleIRIShortFormProvider(); 

	public SimpleEntityDisambiguation(Knowledgebase knowledgebase) {
		this.knowledgebase = knowledgebase;
	}
	
	public Map<Template, Map<Slot, Collection<Entity>>> performEntityDisambiguation(Collection<Template> templates){
		Map<Template, Map<Slot, Collection<Entity>>> template2Allocations = new HashMap<Template, Map<Slot,Collection<Entity>>>();
		
		for(Template template : templates){
			Map<Slot, Collection<Entity>> slot2Entities = performEntityDisambiguation(template);
			template2Allocations.put(template, slot2Entities);
		}
		
		return template2Allocations;
	}

	public Map<Slot, Collection<Entity>> performEntityDisambiguation(Template template){
		Map<Slot, Collection<Entity>> slot2Entities = new HashMap<Slot, Collection<Entity>>();
		List<Slot> slots = template.getSlots();
		for(Slot slot : slots){
			Collection<Entity> candidateEntities = getCandidateEntities(slot);
			slot2Entities.put(slot, candidateEntities);
		}
		return slot2Entities;
	}
	
	private Collection<Entity> getCandidateEntities(Slot slot){
		logger.debug("Generating entity candidates for slot " + slot + "...");
		Set<Entity> candidateEntities = new HashSet<Entity>();
		if(slot.getSlotType() == SlotType.RESOURCE){
			List<String> words = slot.getWords();
			List<Resource> uriCandidates = new ArrayList<Resource>();
			for(String word : words){
				uriCandidates.addAll(UriDisambiguation.getUri(UriDisambiguation.getUriCandidates(word, "en"), word, "en"));
			}
			for (Resource resource : uriCandidates) {
				candidateEntities.add(new Entity(resource.uri, resource.label));
			}
		} else {
			Index index = getIndexForSlot(slot);
			List<String> words = slot.getWords();
			for(String word : words){
				IndexResultSet rs = index.getResourcesWithScores(word, 10);
				for(IndexResultItem item : rs.getItems()){
					String uri = item.getUri();
					String label = item.getLabel();
					if(label == null){
						label = iriSfp.getShortForm(IRI.create(uri));
					}
					candidateEntities.add(new Entity(uri, label));
				}
			}
		}
		logger.debug("Found " + candidateEntities.size() + " entities.");
		logger.debug(candidateEntities);
		return candidateEntities;
	}
	
	private Index getIndexForSlot(Slot slot){
		Index index = null;
		SlotType type = slot.getSlotType();
		if(type == SlotType.CLASS){
			index = knowledgebase.getClassIndex();
		} else if(type == SlotType.PROPERTY || type == SlotType.SYMPROPERTY){
			index = knowledgebase.getPropertyIndex();
		} else if(type == SlotType.DATATYPEPROPERTY){
			index = knowledgebase.getPropertyIndex();
			if(index instanceof VirtuosoPropertiesIndex){
				index = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)index);
			} else if (index instanceof SPARQLPropertiesIndex){
				index = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)index);
			} else if(index instanceof HierarchicalIndex){
				Index primaryIndex = ((HierarchicalIndex)index).getPrimaryIndex();
				if(primaryIndex instanceof VirtuosoPropertiesIndex){
					primaryIndex = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)primaryIndex);
				} else if(primaryIndex instanceof SPARQLPropertiesIndex){
					primaryIndex = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)primaryIndex);
				}
				Index secondaryIndex = ((HierarchicalIndex)index).getSecondaryIndex();
				if(secondaryIndex instanceof VirtuosoPropertiesIndex){
					secondaryIndex = new VirtuosoDatatypePropertiesIndex((SPARQLPropertiesIndex)secondaryIndex);
				} else if(primaryIndex instanceof SPARQLPropertiesIndex){
					secondaryIndex = new SPARQLDatatypePropertiesIndex((SPARQLPropertiesIndex)secondaryIndex);
				}
				index = new HierarchicalIndex(primaryIndex, secondaryIndex);
			}
		} else if(type == SlotType.OBJECTPROPERTY){
			index = knowledgebase.getPropertyIndex();
			if(index instanceof VirtuosoPropertiesIndex){
				index = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)index);
			} else if (index instanceof SPARQLPropertiesIndex){
				index = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)index);
			} else if(index instanceof HierarchicalIndex){
				Index primaryIndex = ((HierarchicalIndex)index).getPrimaryIndex();
				if(primaryIndex instanceof VirtuosoPropertiesIndex){
					primaryIndex = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)primaryIndex);
				} else if(primaryIndex instanceof SPARQLPropertiesIndex){
					primaryIndex = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)primaryIndex);
				}
				Index secondaryIndex = ((HierarchicalIndex)index).getSecondaryIndex();
				if(secondaryIndex instanceof VirtuosoPropertiesIndex){
					secondaryIndex = new VirtuosoObjectPropertiesIndex((SPARQLPropertiesIndex)secondaryIndex);
				} else if(primaryIndex instanceof SPARQLPropertiesIndex){
					secondaryIndex = new SPARQLObjectPropertiesIndex((SPARQLPropertiesIndex)secondaryIndex);
				}
				index = new HierarchicalIndex(primaryIndex, secondaryIndex);
			}
		} else if(type == SlotType.RESOURCE || type == SlotType.UNSPEC){
			index = knowledgebase.getResourceIndex();
		}
		return index;
	}

}
