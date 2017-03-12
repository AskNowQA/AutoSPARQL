package org.aksw.autosparql.widgetset.client;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.VTextField;
import com.vaadin.shared.ui.Connect;
import org.aksw.autosparql.widget.NumberField;

@Connect(NumberField.class)
public class NumberFieldConnector extends AbstractExtensionConnector {
	private static final long serialVersionUID = -737765038361894693L;

	private VTextField textField;
	private KeyPressHandler keyPressHandler = new KeyPressHandler() {
		@Override
		public void onKeyPress(KeyPressEvent event) {
			if (textField.isReadOnly() || !textField.isEnabled()) {
				return;
			}
			int keyCode = event.getNativeEvent().getKeyCode();
			switch (keyCode) {
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_BACKSPACE:
				case KeyCodes.KEY_DELETE:
				case KeyCodes.KEY_TAB:
				case KeyCodes.KEY_UP:
				case KeyCodes.KEY_DOWN:
				case KeyCodes.KEY_SHIFT:
					return;
			}
			if (!isValueValid(event)) {
				textField.cancelKey();
			}
		}
	};

	@Override
	protected void extend(ServerConnector target) {
		textField = (VTextField) ((ComponentConnector) target).getWidget();
		textField.addKeyPressHandler(keyPressHandler);
	}

	private boolean isValueValid(KeyPressEvent event) {
		String newText = getFieldValueAsItWouldBeAfterKeyPress(event.getCharCode());
		try {
			parseValue(newText);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	protected long parseValue(String value) {
		return Long.valueOf(value);
	}

	private String getFieldValueAsItWouldBeAfterKeyPress(char charCode) {
		int index = textField.getCursorPos();
		String previousText = textField.getText();
		StringBuffer buffer = new StringBuffer();
		buffer.append(previousText.substring(0, index));
		buffer.append(charCode);
		if (textField.getSelectionLength() > 0) {
			buffer.append(previousText.substring(index + textField.getSelectionLength(),
												 previousText.length()));
		} else {
			buffer.append(previousText.substring(index, previousText.length()));
		}
		return buffer.toString();
	}
}