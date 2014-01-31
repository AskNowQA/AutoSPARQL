package org.aksw.autosparql.tbsl.gui.vaadin.widget;

import java.util.ArrayList;
import java.util.List;
import org.aksw.autosparql.tbsl.gui.vaadin.model.BasicResultItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.BaseTheme;

public class DBpediaInfoLabel extends HorizontalLayout implements InfoLabel{
	
	private List<FeedBackListener> listeners = new ArrayList<FeedBackListener>();
	private BasicResultItem item;
	
	public DBpediaInfoLabel(BasicResultItem item) {
		this.item = item;
		setWidth(null);
		
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
		
		
		String label = item.getLabel();
		if(label == null){
			label = item.getUri();
		}
		String s1 = "<div><h3><b><a class=\"noline\" href=\"" + item.getUri() +"\" target=\"_blank\">" + label + "</a></b></h3></div>";
		if(item.getImageURL() != null){
			s1 += "<div style='float: right; height: 100px; width: 200px'>" +
	    	 		"<div style='height: 100%;'><a href=\"" + item.getImageURL() + "\" target=\"_blank\"><img style='height: 100%;' src=\"" + item.getImageURL() + "\"/></a></div>" +
	    	 		"</div>";
		}
		if(item.getDescription() != null){
			s1 += "<div>" + item.getDescription() + "</div>";
		}
		
		Label l1 = new Label(s1, Label.CONTENT_XHTML);
		l1.addStyleName("wrap");
		l1.setWidth("500px");
		l1.setHeight("150px");
		addComponent(l1);
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
