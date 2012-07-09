package org.autosparql.tbsl.widget;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.autosparql.tbsl.model.BasicResultItem;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;

public class OxfordInfoLabel extends HorizontalLayout implements InfoLabel{
	
	private List<FeedBackListener> listeners = new ArrayList<FeedBackListener>();
	private BasicResultItem item;
	
	
	public OxfordInfoLabel(BasicResultItem item) {
		this.item = item;
		
		addStyleName("tweet");
		
		VerticalLayout buttons = new VerticalLayout();
		buttons.setHeight("100%");
		buttons.addStyleName("buttons");
		Button posExampleButton = new Button();
		posExampleButton.setIcon(new ThemeResource("images/thumb_up.png"));
		posExampleButton.addStyleName(BaseTheme.BUTTON_LINK);
		posExampleButton.setDescription("Click if this entry is definitely correct.");
		posExampleButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				firePositiveExampleSelected();
			}
		});
		buttons.addComponent(posExampleButton);
		Button negExampleButton = new Button();
		negExampleButton.setIcon(new ThemeResource("images/thumb_down.png"));
		negExampleButton.addStyleName(BaseTheme.BUTTON_LINK);
		negExampleButton.setDescription("Click if this entry is definitely wrong.");
		negExampleButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				fireNegativeExampleSelected();
			}
		});
		buttons.addComponent(negExampleButton);
		buttons.setComponentAlignment(posExampleButton, Alignment.MIDDLE_CENTER);
		buttons.setComponentAlignment(negExampleButton, Alignment.MIDDLE_CENTER);
		addComponent(buttons);
		
		String s1 = "<div><h3><b>" + item.getLabel() +"</b></h3></div>";
		if(item.getImageURL() != null){
			s1 += "<div style='float: right; height: 100px; width: 200px'>" +
	    	 		"<div style='height: 100%;'><img style='height: 100%;' src=\"" + item.getImageURL() + "\"/></div>" +
	    	 		"</div>";
		}
		s1 += "<div>" + item.getDescription() + "</div>";
		Label l1 = new Label(s1, Label.CONTENT_XHTML);
		l1.addStyleName("wrap");
		l1.setWidth("500px");
		l1.setHeight("150px");
		addComponent(l1);
		
		String s2 = "";
//		Double price = (Double) item.getData().get("price");
		Double price = (Double) item.getData().get("price");
		if(price != null){
			s2 = "<div><h1><b>"  + NumberFormat.getCurrencyInstance(Locale.UK).format(price) + "</b></h1></div>";
		}
		s2 += "<div style='border-left:5pt solid black;padding-left:5px;'>";
		String street = (String) item.getData().get("street");
		if(street != null){
			try {
				String googleMaps = "https://maps.google.com?q=" + URLEncoder.encode(street, "UTF-8");
				s2 += "<div><b>Street: </b><a href=\"" + googleMaps + "\">"  + street + "</a></div>";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Integer nrOfBedrooms = (Integer) item.getData().get("bedrooms");
		if(nrOfBedrooms != null){
			s2 += "<div><b>#bedrooms: </b>"  + nrOfBedrooms + "</div>";
		}
		Integer nrOfBathrooms = (Integer) item.getData().get("bathrooms");
		if(nrOfBathrooms != null){
			s2 += "<div><b>#bathrooms: </b>"  + nrOfBathrooms + "</div>";
		}
		Integer nrOfReceptions = (Integer) item.getData().get("receptions");
		if(nrOfReceptions != null){
			s2 += "<div><b>#receptions: </b>"  + nrOfReceptions + "</div>";
		}
		s2 += "</div>";
		if(!s2.isEmpty()){
			addComponent(new Label(s2, Label.CONTENT_XHTML));
		}
		
		
	}

	@Override
	public void addFeedBackListener(FeedBackListener l) {
		listeners.add(l);
	}

	@Override
	public void removeFeedBackListener(FeedBackListener l) {
		listeners.remove(l);		
	}
	
	private void firePositiveExampleSelected(){
		for(FeedBackListener l : listeners){
			l.positiveExampleSelected(item);
		}
	}
	
	private void fireNegativeExampleSelected(){
		for(FeedBackListener l : listeners){
			l.negativeExampleSelected(item);
		}
	}

}
