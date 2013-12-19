package org.aksw.autosparql.tbsl.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.aksw.autosparql.algorithm.tbsl.knowledgebase.Knowledgebase;
import org.aksw.autosparql.tbsl.util.FallbackIndex;
import com.vaadin.terminal.Resource;

public class ExtendedKnowledgebase {
	
	private Knowledgebase kb;
	
	private String label;
	
	private String labelPropertyURI;
	private String descriptionPropertyURI;
	private String imagePropertyURI;
	
	private String labelPropertyLanguage;
	
	private boolean allowAdditionalProperties = false;
	
	private InfoTemplate infoTemplate;
	
	private Map<String, String> mandatoryProperties;
	private Map<String, String> optionalProperties;
	
	private Class infoBoxClass;
	
	private String targetVar;
	
	private List<String> exampleQuestions;
	
	private List<String> propertyNamespaces;
	private Set<String> propertyBlackList;
	
	private FallbackIndex fallbackIndex;
	
	private Resource icon;
	
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI) {
		this(kb, labelPropertyURI, descriptionPropertyURI, null);
	}
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI) {
		this.kb = kb;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
	}
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties) {
		this.kb = kb;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
	}
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, InfoTemplate infoTemplate, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties) {
		this.kb = kb;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.infoTemplate = infoTemplate;
		this.label = kb.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
	}
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties, Class infoBoxClass, String targetVar, boolean allowAdditionalProperties) {
		this.kb = kb;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.label = kb.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
		this.infoBoxClass = infoBoxClass;
		this.targetVar = targetVar;
		this.allowAdditionalProperties = allowAdditionalProperties;
	}
	
	public ExtendedKnowledgebase(Knowledgebase kb, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties, Class infoBoxClass, String targetVar, boolean allowAdditionalProperties, List<String> exampleQuestions) {
		this.kb = kb;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.label = kb.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
		this.infoBoxClass = infoBoxClass;
		this.targetVar = targetVar;
		this.allowAdditionalProperties = allowAdditionalProperties;
		this.exampleQuestions = exampleQuestions;
	}
	
	public void setPropertyNamespaces(List<String> propertyNamespaces) {
		this.propertyNamespaces = propertyNamespaces;
	}
	
	public List<String> getPropertyNamespaces() {
		return propertyNamespaces;
	};
	
	public Knowledgebase getKnowledgebase() {
		return kb;
	}
	
	public String getLabelPropertyURI() {
		return labelPropertyURI;
	}
	
	public String getDescriptionPropertyURI() {
		return descriptionPropertyURI;
	}
	
	public String getImagePropertyURI() {
		return imagePropertyURI;
	}
	
	public InfoTemplate getInfoTemplate() {
		return infoTemplate;
	}
	
	public Map<String, String> getMandatoryProperties() {
		return mandatoryProperties;
	}
	
	public Map<String, String> getOptionalProperties() {
		return optionalProperties;
	}
	
	public Class getInfoBoxClass() {
		return infoBoxClass;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getTargetVar() {
		return targetVar;
	}
	
	public boolean isAllowAdditionalProperties() {
		return allowAdditionalProperties;
	}
	
	public void setLabelPropertyLanguage(String labelPropertyLanguage) {
		this.labelPropertyLanguage = labelPropertyLanguage;
	}
	
	public String getLabelPropertyLanguage() {
		return labelPropertyLanguage;
	}
	
	public List<String> getExampleQuestions() {
		return exampleQuestions;
	}
	
	public void setPropertyBlackList(Set<String> propertyBlackList) {
		this.propertyBlackList = propertyBlackList;
	}
	
	public Set<String> getPropertyBlackList() {
		return propertyBlackList;
	}
	
	public void setFallbackIndex(FallbackIndex fallbackIndex) {
		this.fallbackIndex = fallbackIndex;
	}
	
	public FallbackIndex getFallbackIndex() {
		return fallbackIndex;
	}
	
	public void setIcon(Resource icon) {
		this.icon = icon;
	}
	
	public Resource getIcon() {
		return icon;
	}
	
	@Override
	public String toString() {
		return label;
	}
	
}