package org.autosparql.client.widget;

import org.autosparql.shared.Example;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class LiteralRenderer implements GridCellRenderer<Example>
{
	static final boolean SHOW_LANGUAGE_TAG = false;
	
	public LiteralRenderer()
	{

	}

	@Override
	public Object render(Example model, String property, ColumnData config, int rowIndex, int colIndex,
			ListStore<Example> store, Grid<Example> grid)
	{	
		//String imageURL = model.getImageURL();
		String literal = model.get(property);
		// Remove language tag
		if(!SHOW_LANGUAGE_TAG&&literal.contains("@")) {literal = literal.substring(0,literal.lastIndexOf('@'));}
		return new HTML(literal);
	}
}