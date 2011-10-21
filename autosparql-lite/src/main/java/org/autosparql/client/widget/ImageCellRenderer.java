package org.autosparql.client.widget;

import org.autosparql.shared.Example;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.Image;

public class ImageCellRenderer implements GridCellRenderer<Example>{

	Image image;
	boolean shrinkImage = false;
	Integer maxWidth;
	Integer maxHeight;

	public ImageCellRenderer()
	{

	}

	public ImageCellRenderer(Integer maxWidth, Integer maxHeight)
	{
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
			ListStore<Example> store, Grid<Example> grid) {

		String imageURL = model.getImageURL();
		if(image==null||imageURL.isEmpty()){
			return null;
		} else {
			image = new Image(imageURL);
			image.addErrorHandler(new ErrorHandler() {

				@Override
				public void onError(ErrorEvent event) {
					image = null;
				}
			});
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

}
