package org.aksw.autosparql.tbsl.gui.vaadin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.knowledgebase.LocalKnowledgebase;
import org.aksw.autosparql.commons.knowledgebase.RemoteKnowledgebase;
import org.aksw.autosparql.commons.nlp.wordnet.WordNetUnpacker;
import org.aksw.autosparql.tbsl.algorithm.learning.TBSL;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslDbpedia;
import org.aksw.autosparql.tbsl.algorithm.learning.TbslOxford;
import org.aksw.autosparql.tbsl.gui.vaadin.Manager;
import org.aksw.autosparql.tbsl.gui.vaadin.util.FallbackIndex;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.DBpediaInfoLabel;
import org.aksw.autosparql.tbsl.gui.vaadin.widget.OxfordInfoLabel;
import org.aksw.sparql2nl.naturallanguagegeneration.SimpleNLGwithPostprocessing;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;

public class ExtendedTBSL {
	
	private final TBSL tbsl;

	public final SimpleNLGwithPostprocessing nlg;
	
	public static final ExtendedTBSL DBPEDIA = createDBpediaTBSL(); 
	public static final ExtendedTBSL OXFORD = createOxfordTBSL();
	
	private final String label;
	private final String labelPropertyURI;
	private final String descriptionPropertyURI;
	private final String imagePropertyURI;
	
	private final boolean allowAdditionalProperties; // = false	
	
	private final Map<String, String> mandatoryProperties;
	private final Map<String, String> optionalProperties;
	
	private final Class infoBoxClass;
	
	private final String targetVar;
	
	private final List<String> exampleQuestions;
	
	private List<String> propertyNamespaces = Collections.emptyList();
	private String labelPropertyLanguage;
	private Set<String> propertyBlackList = Collections.emptySet();	
	private InfoTemplate infoTemplate = null;
	private FallbackIndex fallbackIndex = null;	
	private Resource icon = null;
	
/*	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI) {
		this(tbsl, labelPropertyURI, descriptionPropertyURI, null);
	}
	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI) {
		this.tbsl = tbsl;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
	}
	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties) {
		this.tbsl = tbsl;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
	}
	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, InfoTemplate infoTemplate, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties) {
		this.tbsl = tbsl;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.infoTemplate = infoTemplate;
		this.label = tbsl.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
	}
	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI, String imagePropertyURI, Map<String, String> mandatoryProperties, Map<String, String> optionalProperties, Class infoBoxClass, String targetVar, boolean allowAdditionalProperties) {
		this.tbsl = tbsl;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.label = tbsl.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
		this.infoBoxClass = infoBoxClass;
		this.targetVar = targetVar;
		this.allowAdditionalProperties = allowAdditionalProperties;
	}
*/	
	public ExtendedTBSL(TBSL tbsl, String labelPropertyURI, String descriptionPropertyURI,String imagePropertyURI,
			Map<String, String> mandatoryProperties, Map<String, String> optionalProperties, Class infoBoxClass,
			String targetVar, boolean allowAdditionalProperties, List<String> exampleQuestions,SimpleNLGwithPostprocessing nlg, Resource icon)
	{
		this.tbsl = tbsl;
		this.labelPropertyURI = labelPropertyURI;
		this.descriptionPropertyURI = descriptionPropertyURI;
		this.imagePropertyURI = imagePropertyURI;
		this.label = tbsl.getLabel();
		this.mandatoryProperties = mandatoryProperties;
		this.optionalProperties = optionalProperties;
		this.infoBoxClass = infoBoxClass;
		this.targetVar = targetVar;
		this.allowAdditionalProperties = allowAdditionalProperties;
		this.exampleQuestions = exampleQuestions;
		this.nlg=nlg;
		this.icon=icon;
	}
	
	public void setPropertyNamespaces(List<String> propertyNamespaces) {
		this.propertyNamespaces = propertyNamespaces;
	}
	
	public List<String> getPropertyNamespaces() {
		return propertyNamespaces;
	};
	
	public TBSL getTBSL() {
		return tbsl;
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

	static private List<String> loadQuestions(InputStream fileInputStream){
		List<String> questions = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
			String question;
			while((question = br.readLine()) != null){
				questions.add(question);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return questions;
	}

	private static Set<String> loadPropertyBlackList(String filename){
		Set<String> uris = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(ExtendedTBSL.class.getClassLoader().getResourceAsStream(filename)));
			String line;
			while((line = br.readLine()) != null){
				uris.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uris;
	}
	
	private static ExtendedTBSL createDBpediaTBSL()
	{
		String infoTemplateHtml = "<div><h3><b>label</b></h3></div>" +
				"<div style='float: right; height: 100px; width: 200px'>" +
				"<div style='height: 100%;'><img style='height: 100%;' src=\"imageURL\"/></div>" +
				"</div>" +
				"<div>description</div>";
		Map<String, String> propertiesMap = new HashMap<String, String>();
		propertiesMap.put("label", "http://www.w3.org/2000/01/rdf-schema#label");
		propertiesMap.put("imageURL", "http://www.w3.org/2000/01/rdf-schema#comment");
		propertiesMap.put("description", "http://xmlns.com/foaf/0.1/depiction");
		InfoTemplate infoTemplate = new InfoTemplate(infoTemplateHtml, null);

		List<String> exampleQuestions = loadQuestions(ExtendedTBSL.class.getClassLoader().getResourceAsStream("dbpedia_example_questions.txt"));
		SimpleNLGwithPostprocessing nlg = new SimpleNLGwithPostprocessing(((RemoteKnowledgebase)TbslDbpedia.INSTANCE.getKnowledgebase()).getEndpoint(),Manager.getInstance().getCacheDir(),WordNetUnpacker.getUnpackedWordNetDir().getAbsolutePath());
		
		ExtendedTBSL eTBSL = new ExtendedTBSL(
				TbslDbpedia.INSTANCE, 
				"http://www.w3.org/2000/01/rdf-schema#label", 
				"http://www.w3.org/2000/01/rdf-schema#comment",
				"http://xmlns.com/foaf/0.1/depiction", null, null, DBpediaInfoLabel.class, "x0", true, exampleQuestions,nlg,new ThemeResource("images/dbpedia_live_logo.png"));
		eTBSL.setLabelPropertyLanguage("en");
		eTBSL.setPropertyNamespaces(Arrays.asList(new String[]{"http://dbpedia.org/ontology/", RDF.getURI(), RDFS.getURI()}));

		Set<String> propertyBlackList = loadPropertyBlackList("dbpedia_property_blacklist.txt");
		eTBSL.setPropertyBlackList(propertyBlackList);

//		FallbackIndex fallback = new SolrIndex(SOLR_SERVER_URI_EN+"dbpedia_resources");
//		ekb.setFallbackIndex(fallback);		

		return eTBSL;	
	}

	private static ExtendedTBSL createOxfordTBSL(){
		String infoTemplateHtml = "<div><h3><b>label</b></h3></div>" +
				"<div style='float: right; height: 100px; width: 200px'>" +
				"<div style='height: 100%;'><img style='height: 100%;' src=\"imageURL\"/></div>" +
				"</div>" +
				"<div>description</div>";
		Map<String, String> propertiesMap = new HashMap<String, String>();
		propertiesMap.put("label", "http://purl.org/goodrelations/v1#name");
		propertiesMap.put("imageURL", "http://xmlns.com/foaf/0.1/depiction");
		propertiesMap.put("description", "http://purl.org/goodrelations/v1#description");
		InfoTemplate infoTemplate = new InfoTemplate(infoTemplateHtml, null);

		Map<String, String> optionalProperties = new HashMap<String, String>();
		optionalProperties.put("bedrooms", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#bedrooms");
		optionalProperties.put("bathrooms", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#bathrooms");
		optionalProperties.put("receptions", "http://diadem.cs.ox.ac.uk/ontologies/real-estate#receptions");
		optionalProperties.put("street", "http://www.w3.org/2006/vcard/ns#street-address");
		optionalProperties.put("locality", "http://www.w3.org/2006/vcard/ns#locality");

		List<String> exampleQuestions = loadQuestions(ExtendedTBSL.class.getClassLoader().getResourceAsStream("oxford_example_questions.txt"));
		String wordnetDir = WordNetUnpacker.getUnpackedWordNetDir().getAbsolutePath();		
		/*@Nonnull */Model model = ((LocalKnowledgebase)TbslOxford.INSTANCE.getKnowledgebase()).getModel();
		SimpleNLGwithPostprocessing nlg = new SimpleNLGwithPostprocessing(model,wordnetDir);
		
		Resource icon = new ThemeResource("images/oxford_logo.gif");
		ExtendedTBSL ekb = new ExtendedTBSL(
				TbslOxford.INSTANCE, 
				"http://purl.org/goodrelations/v1#name", 
				"http://purl.org/goodrelations/v1#description",
				"http://xmlns.com/foaf/0.1/depiction", null, optionalProperties, OxfordInfoLabel.class, "x0", false, exampleQuestions,nlg,icon);
		return ekb;
	}
}