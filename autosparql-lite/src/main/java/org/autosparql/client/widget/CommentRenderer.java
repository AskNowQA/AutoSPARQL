package org.autosparql.client.widget;

import org.autosparql.shared.Example;
import org.autosparql.shared.StringUtils;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class CommentRenderer implements GridCellRenderer<Example>
{
	static final boolean SHOW_LANGUAGE_TAG = false;
	
	public CommentRenderer()
	{

	}

	@Override
	public Object render(Example model, String property, ColumnData config, int rowIndex, int colIndex,
			ListStore<Example> store, Grid<Example> grid)
	{	
		String literal = model.get(property);
		//literal = SafeHtmlUtils.htmlEscape(literal);
		// Remove language tag
		if(!SHOW_LANGUAGE_TAG&&literal.contains("@")) {literal = literal.substring(0,literal.lastIndexOf('@'));}
		
		String shortLiteral = StringUtils.abbreviate(literal,200);
		String title=literal;
		//return new HTML("<div title=\""+title+"\" alt=\""+title+">"+literal+"</div>");
		if(shortLiteral.length()<literal.length())
		{
		return new HTML("<div title="+title+" alt="+title+">"+shortLiteral+"</div>");
		}
		else
		{
			return new HTML(shortLiteral);
		}
	}
}