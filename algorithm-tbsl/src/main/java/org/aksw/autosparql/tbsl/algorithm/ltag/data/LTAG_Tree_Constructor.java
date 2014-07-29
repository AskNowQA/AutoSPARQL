package org.aksw.autosparql.tbsl.algorithm.ltag.data;

import java.io.StringReader;

import org.aksw.autosparql.tbsl.algorithm.ltag.reader.LTAGTreeParser;
import org.aksw.autosparql.tbsl.algorithm.ltag.reader.ParseException;


public class LTAG_Tree_Constructor {

	public TreeNode construct(String string) throws ParseException 
	{
		// new TreeNode interface in fracosem.ltag
		TreeNode tree;
		LTAGTreeParser parser =  new LTAGTreeParser(new StringReader(new String(string)));
		parser.ReInit(new StringReader(new String(string)));		
		tree = parser.Tree();
		
		return tree;
	}
	
	
}
