package org.aksw.autosparql.commons.nlp.ner;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NamedEntityTagNormalizer {

	public final static Map<String,String> NAMED_ENTITY_TAG_MAPPINGS = new HashMap<String,String>();
	
	/**
     * 
     */
    public static final String NAMED_ENTITY_TAG_OTHER = "OTHER";
    
    /**
     * 
     */
    public static final String NAMED_ENTITY_TAG_MISCELLANEOUS = "MISC";
    
    /**
     * 
     */
    public static final String NAMED_ENTITY_TAG_PLACE = "PLACE";
    
    /**
     * 
     */
    public static final String NAMED_ENTITY_TAG_ORGANIZATION = "ORGANIZATION"; 
    
    /**
     * 
     */
    public static final String NAMED_ENTITY_TAG_PERSON = "PERSON";

    /**
     * 
     */
    public static final String NAMED_ENTITY_TAG_QUOTE = "QUOTE";
    
    public static final String NAMED_ENTITY_TAG_DATE = "DATE";
	
	static {
		
		// persons
	    NAMED_ENTITY_TAG_MAPPINGS.put("PER",          NAMED_ENTITY_TAG_PERSON);
	    NAMED_ENTITY_TAG_MAPPINGS.put("PERSON",       NAMED_ENTITY_TAG_PERSON);
		NAMED_ENTITY_TAG_MAPPINGS.put("B-PER",	      NAMED_ENTITY_TAG_PERSON);
		NAMED_ENTITY_TAG_MAPPINGS.put("I-PER",	      NAMED_ENTITY_TAG_PERSON);
		
		// organizations
		NAMED_ENTITY_TAG_MAPPINGS.put("ORGANIZATION", NAMED_ENTITY_TAG_ORGANIZATION);
		NAMED_ENTITY_TAG_MAPPINGS.put("ORG",          NAMED_ENTITY_TAG_ORGANIZATION);
		NAMED_ENTITY_TAG_MAPPINGS.put("B-ORG",	      NAMED_ENTITY_TAG_ORGANIZATION);
		NAMED_ENTITY_TAG_MAPPINGS.put("I-ORG",	      NAMED_ENTITY_TAG_ORGANIZATION);
		
		// places
		NAMED_ENTITY_TAG_MAPPINGS.put("LOCATION",     NAMED_ENTITY_TAG_PLACE);
		NAMED_ENTITY_TAG_MAPPINGS.put("LOC",          NAMED_ENTITY_TAG_PLACE);
		NAMED_ENTITY_TAG_MAPPINGS.put("B-LOC",	      NAMED_ENTITY_TAG_PLACE);
		NAMED_ENTITY_TAG_MAPPINGS.put("I-LOC",	      NAMED_ENTITY_TAG_PLACE);
		
		// misc
		NAMED_ENTITY_TAG_MAPPINGS.put("B-MISC",	      NAMED_ENTITY_TAG_MISCELLANEOUS);
		NAMED_ENTITY_TAG_MAPPINGS.put("I-MISC",	      NAMED_ENTITY_TAG_MISCELLANEOUS);
		NAMED_ENTITY_TAG_MAPPINGS.put("MISC",	      NAMED_ENTITY_TAG_MISCELLANEOUS);
		
		// other
		NAMED_ENTITY_TAG_MAPPINGS.put("O", 			NAMED_ENTITY_TAG_OTHER);
		NAMED_ENTITY_TAG_MAPPINGS.put("DATE", 		NAMED_ENTITY_TAG_DATE);
		
	}
}
