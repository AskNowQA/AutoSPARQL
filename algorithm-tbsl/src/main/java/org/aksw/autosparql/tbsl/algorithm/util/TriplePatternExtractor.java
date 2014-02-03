package org.aksw.autosparql.tbsl.algorithm.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections15.ListUtils;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.util.VarUtils;

public class TriplePatternExtractor extends ElementVisitorBase {
	
	private Set<Triple> triplePattern;
	
	private Set<Triple> candidates;
	
	private boolean inOptionalClause = false;
	
	private int unionCount = 0;
	private int optionalCount = 0;
	private int filterCount = 0;
	
	public Set<Triple> extractTriplePattern(Query query){
		return extractTriplePattern(query, false);
	}
	
	public Set<Triple> extractTriplePattern(Query query, boolean ignoreOptionals){
		triplePattern = new HashSet<Triple>();
		candidates = new HashSet<Triple>();
		
		query.getQueryPattern().visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			if(query.isSelectType()){
				for(Triple t : candidates){
					if(ListUtils.intersection(new ArrayList<Var>(VarUtils.getVars(t)), query.getProjectVars()).size() >= 2){
						triplePattern.add(t);
					}
				}
			}
		}
		
		return triplePattern;
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group){
		return extractTriplePattern(group, false);
	}
	
	public Set<Triple> extractTriplePattern(ElementGroup group, boolean ignoreOptionals){
		triplePattern = new HashSet<Triple>();
		candidates = new HashSet<Triple>();
		
		group.visit(this);
		
		//postprocessing: triplepattern in OPTIONAL clause
		if(!ignoreOptionals){
			for(Triple t : candidates){
				triplePattern.add(t);
			}
		}
		
		return triplePattern;
	}
	
	@Override
	public void visit(ElementGroup el) {
		for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
			Element e = iterator.next();
			e.visit(this);
		}
	}

	@Override
	public void visit(ElementOptional el) {
		optionalCount++;
		inOptionalClause = true;
		el.getOptionalElement().visit(this);
		inOptionalClause = false;
	}

	@Override
	public void visit(ElementTriplesBlock el) {
		for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
			Triple t = iterator.next();
			if(inOptionalClause){
				candidates.add(t);
			} else {
				triplePattern.add(t);
			}
		}
	}

	@Override
	public void visit(ElementPathBlock el) {
		for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
			TriplePath tp = iterator.next();
			if(inOptionalClause){
				candidates.add(tp.asTriple());
			} else {
				triplePattern.add(tp.asTriple());
			}
		}
	}

	@Override
	public void visit(ElementUnion el) {
		unionCount++;
		for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
			Element e = iterator.next();
			e.visit(this);
		}
	}
	
	@Override
	public void visit(ElementFilter el) {
		filterCount++;
	}

	public int getUnionCount() {
		return unionCount;
	}

	public int getOptionalCount() {
		return optionalCount;
	}

	public int getFilterCount() {
		return filterCount;
	}
}
