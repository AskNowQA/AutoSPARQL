/* just integrated this in Indices
package org.aksw.autosparql.commons.index;

import org.dllearner.common.index.SPARQLClassesIndex;
import org.dllearner.common.index.SPARQLDatatypePropertiesIndex;
import org.dllearner.common.index.SPARQLIndex;
import org.dllearner.common.index.SPARQLObjectPropertiesIndex;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class SparqlIndices extends Indices
{
	
 public SparqlIndices(SparqlEndpoint endpoint)
 {
	 super(new SPARQLIndex(endpoint), new SPARQLClassesIndex(endpoint), new SPARQLObjectPropertiesIndex(endpoint), new SPARQLDatatypePropertiesIndex(endpoint));
 }
 
}
*/