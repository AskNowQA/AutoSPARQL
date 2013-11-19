package org.aksw.autosparql.client.widget;

import org.aksw.autosparql.shared.Example;
import org.aksw.autosparql.shared.ResourceImageLinks;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.ui.HTML;

public class LabelRenderer implements GridCellRenderer<Example>
{
	static final boolean SHOW_LANGUAGE_TAG = false;
	
	public LabelRenderer()
	{

	}

	@Override
	public Object render(Example model, String property, ColumnData config, int rowIndex, int colIndex,
			ListStore<Example> store, Grid<Example> grid)
	{	
		//String imageURL = model.getImageURL();
		String literal = model.get(property);
		if(literal==null||literal.isEmpty()) return new HTML();
		// Remove language tag
		if(!SHOW_LANGUAGE_TAG&&literal.contains("@")) {literal = literal.substring(0,literal.lastIndexOf('@'));}
		String dbpediaLink  = "<a target=\"_blank\" href=\""+model.getURI()+"\">"+literal+"</a>";
		final StringBuilder html = new StringBuilder();
		html.append(dbpediaLink + "<br/>");
		
		for(String sameAsLink : model.getSameAsLinks()){
			html.append("<a style='padding-top=5px' target=\"_blank\" href=\""+sameAsLink+"\">"+ResourceImageLinks.getImage(sameAsLink)+"</a> &nbsp;");
		}
		
//		AutoSPARQLService.Util.getInstance().getSameAsLinks(model.getURI(), new AsyncCallback<List<String>>() {
//			
//			@Override
//			public void onSuccess(List<String> result) {
//				for(String sameAsLink : result){
//					html.append("<br/><a target=\"_blank\" href=\""+sameAsLink+"\">"+sameAsLink+"</a>");
//					
//				}
//				
//			}
//			
//			@Override
//			public void onFailure(Throwable caught) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
//		String wikipediaLink = "<a target=\"_blank\" href=\""+model.getURI().replace("dbpedia.org/resource/","en.wikipedia.org/wiki/")+"\">"+"(Go to Wikipedia article)"+"</a>";
		
		return new HTML(html.toString());
	}
}