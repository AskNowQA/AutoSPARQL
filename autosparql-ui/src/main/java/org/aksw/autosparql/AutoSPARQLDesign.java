package org.aksw.autosparql;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.*;
import com.vaadin.ui.declarative.Design;
import de.fatalix.vaadin.addon.codemirror.CodeMirror;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorLanguage;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorScrollbarStyle;
import de.fatalix.vaadin.addon.codemirror.CodeMirrorTheme;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lorenz Buehmann
 */
@DesignRoot
public class AutoSPARQLDesign extends VerticalLayout {

	private static final Pattern EXAMPLES_PATTERN = Pattern.compile("(?<=\\<)(.*?)(?=\\>)");

	protected ComboBox<Dataset> datasetSelector;

	protected TextArea posExamplesInput;
	protected TextArea negExamplesInput;
	protected Button searchPosButton;
	protected Button searchNegButton;

	protected Button runButton;

	protected CodeMirror sparqlQueryField;

	protected Grid<RDFNode> sparqlResultsGrid;

	// settings
	protected SettingsForm settingsForm;

	public AutoSPARQLDesign() {
		Design.read(this);

		sparqlQueryField.setWidth("100%");
//		sparqlQueryField.setHeight(600, Unit.PIXELS);
		sparqlQueryField.setReadOnly(true);
		sparqlQueryField.setCode("");
//        codeMirror.setWidth(1000, Unit.PIXELS);
		sparqlQueryField.setLanguage(CodeMirrorLanguage.SPARQL);
		sparqlQueryField.setTheme(CodeMirrorTheme.DEFAULT);
		sparqlQueryField.setScrollbarStyle(CodeMirrorScrollbarStyle.HIDDEN);

	}

	/**
	 * @return the pos. examples
	 */
	protected List<String> getPosExamples() {
		return parseExamples(posExamplesInput.getValue());
	}

	/**
	 * @return the neg. examples
	 */
	protected List<String> getNegExamples() {
		return parseExamples(negExamplesInput.getValue());
	}

	private static List<String> parseExamples(String text) {
		Matcher matcher = EXAMPLES_PATTERN.matcher(text);

		List<String> examples = new ArrayList<>();

		while (matcher.find()) {
			String uri = matcher.group();

			examples.add(uri);
		}

		return examples;
	}

	/**
	 * Reset the view, i.e. remove SPARQL query, table results, etc.
	 */
	protected void reset() {
		sparqlQueryField.setCode("");
		sparqlResultsGrid.setItems();
	}
}
