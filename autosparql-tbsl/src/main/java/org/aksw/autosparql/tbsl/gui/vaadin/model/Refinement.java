package org.aksw.autosparql.tbsl.gui.vaadin.model;

import java.util.List;

public class Refinement {

	private List<String> posExamples;
	private List<String> negExamples;
	private String sparqlQuery;
	private String refinedSPARQLQuery;
	private Answer refinedAnswer;
	private String questionExample;

	public Refinement(List<String> posExamples, List<String> negExamples, String sparqlQuery,
			String refinedSPARQLQuery, Answer refinedAnswer, String questionExample) {
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		this.sparqlQuery = sparqlQuery;
		this.refinedSPARQLQuery = refinedSPARQLQuery;
		this.refinedAnswer = refinedAnswer;
		this.questionExample = questionExample;
	}

	public List<String> getPosExamples() {
		return posExamples;
	}

	public List<String> getNegExamples() {
		return negExamples;
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}

	public String getRefinedSPARQLQuery() {
		return refinedSPARQLQuery;
	}

	public Answer getRefinedAnswer() {
		return refinedAnswer;
	}

	public String getQuestionExample() {
		return questionExample;
	}


}
