/* just integrated this in Indices
package org.aksw.autosparql.commons.index;

import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import com.hp.hpl.jena.rdf.model.Model;

public class ModelIndices extends Indices
{
	
 public ModelIndices(Model model)
 {
	 super(new SPARQLIndex(model), new SPARQLClassesIndex(model), new SPARQLObjectPropertiesIndex(model), new SPARQLDatatypePropertiesIndex(model));
 }
 
}
*/