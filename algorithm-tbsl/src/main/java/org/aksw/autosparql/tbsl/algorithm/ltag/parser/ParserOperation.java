package org.aksw.autosparql.tbsl.algorithm.ltag.parser;

import java.util.List;

/** Interface for Parser Operations such as LeftPredictor, RightCompletor etc. **/

interface ParserOperation {

	public List<ParseState> go(int i, ParseState state, String[] input,
			ParseGrammar G);

}