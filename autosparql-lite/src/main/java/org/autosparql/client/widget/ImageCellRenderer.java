package org.autosparql.client.widget;

import java.util.Set;
import java.util.logging.Logger;

import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class ImageCellRenderer implements GridCellRenderer<Example>{
	boolean shrinkImage = false;
	Integer maxWidth;
	Integer maxHeight;
	private static final Logger log = Logger.getLogger(SearchResultPanel.class.toString());

	protected final Set<String> imageProperties;

	public ImageCellRenderer(Set<String> imageProperties, Integer maxWidth, Integer maxHeight)
	{
		this.imageProperties=imageProperties;
		if(maxWidth==null||maxHeight==null||maxWidth<=0||maxHeight<=0)
		{
			this.maxHeight=null;
			this.maxWidth=null;
		} else
		{
			shrinkImage = true;
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
		}
	}

	@Override
	public Object render(Example model, String property, ColumnData config, int rowIndex, int colIndex,
			ListStore<Example> store, Grid<Example> grid)
	{	
		boolean containsImage = false;
		String imageURL=null;
		Image image = null;
		for(String imageProperty: imageProperties)
		{
			imageURL = model.get(property);
			if(imageURL==null||imageURL.isEmpty()) {continue;}
			image = new Image(imageURL);
//			if(image==null)
//			{
//				log.warning("Error rendering URL <br/>\""+imageURL+"\"");
//				continue;
//			}
			containsImage = true;
			break;
		}
		if(!containsImage){return new HTML("");}//{return new HTML("No image");}
//		if(image==null){return new HTML("Error rendering URL <br/>\""+imageURL+"\"");}

		log.info("rendering "+imageURL);
	
		//		} else {
		//			image = new Image(imageURL);
		//			image.addErrorHandler(new ErrorHandler() {
		//
		//				@Override
		//				public void onError(ErrorEvent event) {
		//					image = null;
		//				}
		//			});
		if(shrinkImage&&image != null&&(image.getWidth()>maxWidth||image.getHeight()>maxHeight))
		{
			//double aspectRatio = image.getWidth() / image.getHeight();
			double shrinkFactor = Math.min((double)maxWidth/image.getWidth(),(double)maxHeight/image.getHeight());
			image.setPixelSize((int)((double)image.getWidth()*shrinkFactor),(int)((double)image.getHeight()*shrinkFactor));
			//image.setPixelSize(40, 40);
		}
		return image;
	}
}