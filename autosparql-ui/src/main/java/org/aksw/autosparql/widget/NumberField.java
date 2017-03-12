package org.aksw.autosparql.widget;

import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.TextField;


public class NumberField extends AbstractExtension {

	public static void extend(TextField field) {
		new NumberField().extend((AbstractClientConnector) field);
	}
}