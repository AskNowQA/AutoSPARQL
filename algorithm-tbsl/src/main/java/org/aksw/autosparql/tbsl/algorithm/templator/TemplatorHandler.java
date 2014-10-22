package org.aksw.autosparql.tbsl.algorithm.templator;

import java.util.Set;

import org.aksw.autosparql.tbsl.algorithm.sparql.BasicQueryTemplate;
import org.aksw.autosparql.tbsl.algorithm.sparql.Template;

public class TemplatorHandler {

    Templator templator;
    BasicTemplator basictemplator;

    public TemplatorHandler(String[] files) {
    	templator = new Templator();
    	basictemplator = new BasicTemplator();
    	templator.setGrammarFiles(files);
        basictemplator.setGrammarFiles(files);
    }

    public void setVerbose(boolean b) {
        templator.setVERBOSE(b);
    }

    public Set<Template> buildTemplates(String s) {
   		return templator.buildTemplates(s);
    }

    public Set<BasicQueryTemplate> buildBasicTemplates(String s) {
    	return basictemplator.buildBasicQueries(s);
    }

}
