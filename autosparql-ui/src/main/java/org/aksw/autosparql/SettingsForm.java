package org.aksw.autosparql;

import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;
import com.vaadin.ui.declarative.Design;
import org.aksw.autosparql.widget.NumberField;

/**
 * The AutoSPARQL settings form.
 *
 * @author Lorenz Buehmann
 */
@DesignRoot
public class SettingsForm extends FormLayout {

	protected Slider maxDepthSlider;
	protected CheckBox useMaxExecutionTimeCB;
	protected TextField maxExecutionTimeField;
	protected CheckBox useInferenceCB;
	protected CheckBox useIncomingDataCB;
	protected CheckBox minimizeQueryCB;

	public SettingsForm() {
		Design.read(this);

		// max depth between 1 and 3
		maxDepthSlider.setMin(1);
		maxDepthSlider.setMax(3);

		// allow only numeric data
		NumberField.extend(maxExecutionTimeField);
		maxExecutionTimeField.setValue("10");
		useMaxExecutionTimeCB.addValueChangeListener((e) -> maxExecutionTimeField.setEnabled(e.getValue()));

//		CheckBox cb = new CheckBox("Test");
//		ContextHelp help = new ContextHelp();
//		help.addHelpForComponent(cb, "Help test");
//		addComponent(new HelpFieldWrapper<>(cb, help));
	}

	protected int getMaxDepth() {
		return maxDepthSlider.getValue().intValue();
	}

	protected int getMaxExecutionTime() {
		return useMaxExecutionTimeCB.getValue() ? Integer.parseInt(maxExecutionTimeField.getValue()) : -1;
	}

	protected boolean isUseInference() {
		return useInferenceCB.getValue();
	}

	protected boolean isUseIncomingData() {
		return useIncomingDataCB.getValue();
	}

	protected boolean isMinimizeQuery() {
		return minimizeQueryCB.getValue();
	}
}
