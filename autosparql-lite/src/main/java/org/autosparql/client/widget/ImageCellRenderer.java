package org.autosparql.client.widget;

import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

public class ImageCellRenderer implements GridCellRenderer<Example>{
	static int imageNr = 0;
	boolean shrinkImage = true;
	Integer maxWidth;
	Integer maxHeight;
	private static final Logger log = Logger.getLogger(ImageCellRenderer.class.toString());
	
	private Image image;

	protected final Set<String> imageProperties;
	
	private boolean paint = true;

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
		String imageURL2 = null;
		for(String imageProperty: imageProperties){
			final String imageURL = model.get(imageProperty);
			
			if(imageURL != null && !imageURL.isEmpty()) {
				image = new Image();
				image.addErrorHandler(new ErrorHandler() {

					@Override
					public void onError(ErrorEvent event) {
						paint = false;
						log.info("Couldn't load image " + imageURL);
					}
				});
				image.setUrl(imageURL);
				if(image != null){
					imageURL2 = imageURL;
					containsImage = true;
					break;
				}
			}
		}
		
		/*
		for(String imageProperty: imageProperties)
		{
			imageURL = model.get(property);
			if(imageURL==null||imageURL.isEmpty()) {continue;}
			image.setUrl(imageURL);
			if(image != null){
				containsImage = true;
				break;
			}
//			if(image==null)
//			{
//				log.warning("Error rendering URL <br/>\""+imageURL+"\"");
//				continue;
//			}
		}*/
		if(!containsImage){System.out.println("NOT");return new HTML("");}//{return new HTML("No image");}
//		if(image==null){return new HTML("Error rendering URL <br/>\""+imageURL+"\"");}

		log.info("rendering "+imageURL2);
		System.out.println(image);
		// not loaded yet, cannot retrieve width
		if(image.getWidth()==0){return "<img src=\"" + imageURL2 + "\" id=\"image"+(++imageNr)+"\" onload=\"resizeToMax(this.id)\"/>";}
		if(shrinkImage&&image != null&&(image.getWidth()>maxWidth||image.getHeight()>maxHeight))
		{
			//double aspectRatio = image.getWidth() / image.getHeight();
			double shrinkFactor = Math.min((double)maxWidth/image.getWidth(),(double)maxHeight/image.getHeight());
			image.setPixelSize((int)((double)image.getWidth()*shrinkFactor),(int)((double)image.getHeight()*shrinkFactor));
			log.info("Shrinking image " + imageURL2 + "| Factor: " + shrinkFactor);
		}
//		String imageURL2 = "http://images2.wikia.nocookie.net/__cb20100923201744/uncyclopedia/images/thumb/6/63/Wikipedia-logo.png/98px-Wikipedia-logo.png";
//		return "<img src=\"" + imageURL + "\" alt=\"no image\"/>";
		return image;
	}
	
	
}