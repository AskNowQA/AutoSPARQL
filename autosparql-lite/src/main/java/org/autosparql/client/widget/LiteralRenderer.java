package org.autosparql.client.widget;

import org.autosparql.shared.Example;
import org.autosparql.shared.StringUtils;

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
		if(literal==null) {return null;}
		if(literal.startsWith("http://dbpedia.org/resource/"))
		{
			String link = literal.replace("http://dbpedia.org/resource/", "http://en.wikipedia.org/wiki/");
			String show = literal.replace("http://dbpedia.org/resource/", "").replace("_"," "); 
			literal= "<a target=\"_blank\" href=\""+link+"\">"+show+"</a>";	
		}
		else if(literal.contains("^^"))
		{
			// datatype property
			literal = literal.substring(0,literal.indexOf('^'));
		}
		// Remove language tag
		else
		{
			if(!SHOW_LANGUAGE_TAG&&literal.contains("@")) {literal = literal.substring(0,literal.lastIndexOf('@'));}
			literal = StringUtils.abbreviate(literal,200);
		}
		return new HTML(literal);
	}
}