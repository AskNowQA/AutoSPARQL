package org.autosparql.tbsl.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.autosparql.tbsl.Manager;
import org.autosparql.tbsl.TBSLManager;
import org.autosparql.tbsl.UserSession;
import org.autosparql.tbsl.model.Answer;
import org.autosparql.tbsl.model.BasicResultItem;
import org.autosparql.tbsl.model.ExtendedKnowledgebase;
import org.autosparql.tbsl.model.Refinement;
import org.autosparql.tbsl.model.SelectAnswer;
import org.autosparql.tbsl.model.SortProperty;
import org.autosparql.tbsl.util.EscapeUtils;
import org.autosparql.tbsl.util.JENAUtils;
import org.autosparql.tbsl.util.Labels;
import org.autosparql.tbsl.widget.Charts;
import org.autosparql.tbsl.widget.FeedBackListener;
import org.autosparql.tbsl.widget.InfoLabel;
import org.autosparql.tbsl.widget.OxfordPriceChartWindow;
import org.autosparql.tbsl.widget.TBSLProgressListener;
import org.vaadin.appfoundation.view.View;
import org.vaadin.appfoundation.view.ViewContainer;
import org.vaadin.sasha.portallayout.PortalLayout;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.Refresher.RefreshListener;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.AbstractSelect.Filtering;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.HeaderClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.themes.BaseTheme;

public class MainView extends VerticalLayout implements ViewContainer, TBSLProgressListener{
	
	private static final int REFRESH_INTERVAL = 1000;
	
	private VerticalLayout mainPanel;
	private View currentView;
	
	private Embedded knowledgebaseLogo;
	private NativeSelect knowledgebaseSelector;
	
	private NativeButton executeButton;
	private TextField questionField;
	private ComboBox questionBox;
	
	private VerticalLayout resultHolderPanel;
	private Table resultTable;
	private ComboBox propertySelector;
	
	private HorizontalLayout feedbackPanel;
	private Label feedbackLabel;
	private NativeButton wrongSolutionButton;
	
	
	private boolean executing = false;
	private Answer answer;
	
	private Refresher refresher;
	
	private Button refineButton;
	
	private List<Object> positiveMarkedRows;
	private List<Object> negativeMarkedRows;
	
	VerticalLayout l;
	
	public MainView() {
		setSizeFull();
		
//		createHeader();
		createMainPanel();
//		createFooter();
		
		reset();
		UserSession.getManager().setProgressListener(this);
		
		refresher = new Refresher();
		refresher.setRefreshInterval(REFRESH_INTERVAL);
		refresher.setEnabled(false);
	    refresher.addListener(new RefreshListener() {
			@Override
			public void refresh(Refresher source) {
				if (!executing) {
			        // stop polling
			        source.setEnabled(false);
			        executeButton.setEnabled(true);
			        showAnswer(answer);
			      }
				
			}
		});
	   addComponent(refresher);
	}
	
	@Override
	public void attach() {
		createFooter();
	}
	
	public void initWithParams(String endpoint, String question){
		System.out.println("init with params");
		if(endpoint.equals("dbpedia")){
			knowledgebaseSelector.select(UserSession.getManager().getKnowledgebases().get(1));
		} else if(endpoint.equals("oxford")){
			knowledgebaseSelector.select(UserSession.getManager().getKnowledgebases().get(0));
		}
		questionBox.addItem(question);
		questionBox.setValue(question);
		onExecuteQuery();
		
	}
	
	private void createHeader(){
		HorizontalLayout header = new HorizontalLayout();
		header.addStyleName("header");
		header.setWidth("100%");
		header.setHeight(null);
		addComponent(header);
		
		Resource res = new ThemeResource("images/sparql2nl_logo.gif");
	    Label pad = new Label();
		pad.setWidth("100%");
		pad.setIcon(res);
		pad.addStyleName("blub");
//		header.addComponent(pad);
//		header.setExpandRatio(pad, 1f);
	}
	
	private void createFooter(){
		try {
			CustomLayout footer = new CustomLayout(this.getClass().getClassLoader().getResourceAsStream("footer.html"));
			footer.addStyleName("footer");
			footer.setWidth("100%");
			footer.setHeight(null);
			addComponent(footer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createMainPanel(){
		mainPanel = new VerticalLayout();
		mainPanel.setSpacing(true);
		mainPanel.setSizeFull();
		addComponent(mainPanel);
		setExpandRatio(mainPanel, 1f);
		
		//top part
		Component inputForm = createInputComponent();
		inputForm.setWidth("60%");
		inputForm.setHeight("100%");
		VerticalLayout inputFormHolder = new VerticalLayout();
		inputFormHolder.addStyleName("input-form");
		inputFormHolder.setHeight(null);
		inputFormHolder.setWidth("100%");
		inputFormHolder.addComponent(inputForm);
		inputFormHolder.setComponentAlignment(inputForm, Alignment.MIDDLE_CENTER);
		mainPanel.addComponent(inputFormHolder);
		mainPanel.setComponentAlignment(inputFormHolder, Alignment.MIDDLE_CENTER);
		
		//middle part
		resultHolderPanel = new VerticalLayout();
		resultHolderPanel.setWidth("80%");
		resultHolderPanel.setHeight("100%");
		mainPanel.addComponent(resultHolderPanel);
		mainPanel.setComponentAlignment(resultHolderPanel, Alignment.MIDDLE_CENTER);
		mainPanel.setExpandRatio(resultHolderPanel, 0.8f);
		
		//refine button, only visible if constraints running QTL algorithm are satisfied
		refineButton = new Button("Refine");
		refineButton.setVisible(false);
		refineButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				onRefineResult();
			}
		});
		mainPanel.addComponent(refineButton);
		mainPanel.setComponentAlignment(refineButton, Alignment.MIDDLE_CENTER);
		
		
	}
	
	private Component createInputComponent(){
		HorizontalLayout l = new HorizontalLayout();
		l.setSpacing(true);
		
		Component kbSelector = createKnowledgebaseComponent();
		kbSelector.setWidth("150px");
		kbSelector.setHeight("100px");
		l.addComponent(kbSelector);
		
		VerticalLayout right = new VerticalLayout();
		right.setSizeFull();
		right.setHeight("100px");
		right.setSpacing(true);
		l.addComponent(right);
		l.setExpandRatio(right, 1f);
		HorizontalLayout rightTop = new HorizontalLayout();
		rightTop.setSpacing(true);
		rightTop.setHeight(null);
		rightTop.setWidth("100%");
		right.addComponent(rightTop);
		questionBox = new ComboBox();
		questionBox.setWidth("100%");
		questionBox.setHeight(null);
		questionBox.setImmediate(true);
		questionBox.setNewItemsAllowed(true);
		questionBox.setInputPrompt("Enter your question.");
		questionBox.addListener(new Property.ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				onExecuteQuery();
			}
		});
		questionBox.addShortcutListener(new ShortcutListener("run", ShortcutAction.KeyCode.ENTER, null) {
		    @Override
		    public void handleAction(Object sender, Object target) {
		       onExecuteQuery();
		    }
		});
		rightTop.addComponent(questionBox);
		rightTop.setExpandRatio(questionBox, 1f);
		
		addExampleQuestions();
		
		executeButton = new NativeButton("Run");
		executeButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				onExecuteQuery();
			}
		});
		rightTop.addComponent(executeButton);
		
		Component feedbackComponent = createFeedbackComponent();
		right.addComponent(feedbackComponent);
		right.setExpandRatio(feedbackComponent, 1f);
		right.setComponentAlignment(feedbackComponent, Alignment.MIDDLE_CENTER);
		
		
		return l;
	}
	
	private Component createFeedbackComponent(){
		feedbackPanel = new HorizontalLayout();
		feedbackPanel.setSpacing(true);
		feedbackPanel.setSizeFull();
		feedbackPanel.addStyleName("white-border");
		feedbackPanel.addStyleName("shadow-label");
		feedbackPanel.setWidth("95%");
		feedbackPanel.setHeight("80%");
		feedbackPanel.setVisible(false);
		
		feedbackLabel = new Label("&nbsp;", Label.CONTENT_XHTML);
		feedbackLabel.setContentMode(Label.CONTENT_XHTML);
		feedbackLabel.addStyleName("status-label");
		feedbackLabel.setSizeFull();
		feedbackPanel.addComponent(feedbackLabel);
		feedbackPanel.setExpandRatio(feedbackLabel, 1f);
		feedbackPanel.setComponentAlignment(feedbackLabel, Alignment.MIDDLE_CENTER);
		
		wrongSolutionButton = new NativeButton("Wrong!");
		wrongSolutionButton.setWidth("60px");
		wrongSolutionButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Window otherSolutionsWindow = createOtherSolutionsWindow();
				getApplication().getMainWindow().addWindow(otherSolutionsWindow);
			}
		});
		wrongSolutionButton.addStyleName("shift-left");
//		wrongSolutionButton.setVisible(false);
//		feedbackPanel.addComponent(wrongSolutionButton);
//		feedbackPanel.setComponentAlignment(wrongSolutionButton, Alignment.MIDDLE_CENTER);
		
		return feedbackPanel;
	}
	
	private Window createOtherSolutionsWindow(){
		final Window otherSolutionsWindow = new Window();
		otherSolutionsWindow.setWidth("700px");
		otherSolutionsWindow.setHeight("700px");
		otherSolutionsWindow.addListener(new Window.CloseListener() {

			@Override
			public void windowClose(CloseEvent e) {
				MainView.this.getApplication().getMainWindow().removeWindow(otherSolutionsWindow);
			}
		});
		
		VerticalLayout content = new VerticalLayout();
		content.setSizeFull();
		content.setSpacing(true);
		otherSolutionsWindow.setContent(content);

		Label label = new Label("Did you mean?");
		label.setHeight(null);
		label.addStyleName("did-you-mean-header");
		content.addComponent(label);
		
		final List<Entry<String, String>> otherSolutions = UserSession.getManager().getMoreSolutions();
		final Table table = new Table();
		table.setSizeFull();
		table.setImmediate(true);
		table.addContainerProperty("solution", Label.class, null);
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		content.addComponent(table);
		content.setExpandRatio(table, 1f);
		
		Item item;
		for(Entry<String, String> sol : otherSolutions){
			item = table.addItem(sol);
			item.getItemProperty("solution").setValue(sol);
		}
		table.addGeneratedColumn("solution", new ColumnGenerator() {
			@Override
			public Component generateCell(Table source, final Object itemId, Object columnId) {
				final Entry<String, String> entry = (Entry<String, String>) itemId;
				
				HorizontalLayout c = new HorizontalLayout();
				c.setHeight("30px");
				c.setWidth(null);
				c.addStyleName("tweet");
				
				VerticalLayout buttons = new VerticalLayout();
				buttons.setHeight("100%");
				buttons.addStyleName("buttons");
				Button posExampleButton = new Button();
				posExampleButton.setIcon(new ThemeResource("images/thumb_up.png"));
				posExampleButton.addStyleName(BaseTheme.BUTTON_LINK);
				posExampleButton.setDescription("Click if this is what you intended.");
				posExampleButton.addListener(new Button.ClickListener() {
					
					@Override
					public void buttonClick(ClickEvent event) {
						MainView.this.getApplication().getMainWindow().removeWindow(otherSolutionsWindow);
						Answer newAnswer = UserSession.getManager().createAnswer(entry.getKey());
						resultHolderPanel.removeAllComponents();
						showAnswer(newAnswer);
					}
				});
				buttons.addComponent(posExampleButton);
				c.addComponent(buttons);
				Label solutionLabel = new Label(entry.getValue());
				solutionLabel.addStyleName("other-solution-label");
				c.addComponent(solutionLabel);
				return c;
			}
		});
		
		//more button
		final Button moreButton = new Button("More");
		moreButton.setHeight(null);
		moreButton.addListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				List<Entry<String, String>> moreOtherSolutions = UserSession.getManager().getMoreSolutions(otherSolutions.size()+1);
				if(moreOtherSolutions.size() < 10){
					moreButton.setEnabled(false);
				}
				Item item;
				for(Entry<String, String> sol : moreOtherSolutions){
					item = table.addItem(sol);
					item.getItemProperty("solution").setValue(sol);
				}
				otherSolutions.addAll(moreOtherSolutions);
			}
		});
		content.addComponent(moreButton);
		content.setComponentAlignment(moreButton, Alignment.MIDDLE_CENTER);
		return otherSolutionsWindow;
	}
	
	private Component createKnowledgebaseComponent(){
		l = new VerticalLayout();
		l.setSpacing(true);
		l.setSizeFull();
		
		IndexedContainer ic = new IndexedContainer();
		ic.addContainerProperty("label", String.class, null);
		
		for(ExtendedKnowledgebase ekb : UserSession.getManager().getKnowledgebases()){
			ic.addItem(ekb).getItemProperty("label").setValue(ekb.getLabel());
		}
        
		knowledgebaseSelector = new NativeSelect();
		knowledgebaseSelector.addStyleName("borderless");
		knowledgebaseSelector.setWidth("100%");
		knowledgebaseSelector.setHeight(null);
		knowledgebaseSelector.setNullSelectionAllowed(false);
		knowledgebaseSelector.setContainerDataSource(ic);
		knowledgebaseSelector.setImmediate(true);
		knowledgebaseSelector.addListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				onChangeKnowledgebase();
			}
		});
        l.addComponent(knowledgebaseSelector);
        
		knowledgebaseLogo = new Embedded("");
		knowledgebaseLogo.setType(Embedded.TYPE_IMAGE);
		knowledgebaseLogo.setHeight("50px");
		l.addComponent(knowledgebaseLogo);
		l.setComponentAlignment(knowledgebaseLogo, Alignment.MIDDLE_CENTER);
		l.setExpandRatio(knowledgebaseLogo, 1f);
		
        return l;
	}
	
	private void addExampleQuestions(){
		questionBox.removeAllItems();
		List<String> exampleQuestions = UserSession.getManager().getCurrentExtendedKnowledgebase().getExampleQuestions();
		for(String question : exampleQuestions){
			questionBox.addItem(question);
		}
	}
	
	public void reset(){
		knowledgebaseSelector.setValue(UserSession.getManager().getCurrentExtendedKnowledgebase());
	}
	
	private void onChangeKnowledgebase(){
		ExtendedKnowledgebase ekb = (ExtendedKnowledgebase) knowledgebaseSelector.getValue();
		if(ekb.getIcon() != null){
			knowledgebaseLogo.setSource(ekb.getIcon());
			knowledgebaseLogo.setDescription(ekb.getKnowledgebase().getDescription());
		}
		UserSession.getManager().setKnowledgebase(ekb);
		addExampleQuestions();
	}
	
	
	
	private void onExecuteQuery(){
		resultHolderPanel.removeAllComponents();
		final String question = (String) questionBox.getValue();
		if(question != null){
			executeButton.setEnabled(false);
			resetFeedback();
			showFeedback(true);
			final TBSLManager man = UserSession.getManager();
			executing = true;
			refresher.setEnabled(true);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					answer = man.answerQuestion(question);
					executing = false;
				}
			}).start();
		}
//		showAnswer(answer);
	}
	
	private void onAddTableColumn(final String propertyURI){
//		UserSession.getManager().fillItems(propertyURI);
		Object[] visibleColumnsArray = resultTable.getVisibleColumns();
		List<Object> visibleColumns;
		if(visibleColumnsArray == null){
			visibleColumns = new ArrayList<Object>();
			visibleColumns.add("object");
		} else {
			visibleColumns = new ArrayList<Object>(Arrays.asList(visibleColumnsArray));
		}
		resultTable.addContainerProperty(propertyURI, Object.class, null);
		for(Object itemId : resultTable.getItemIds()){
			resultTable.getItem(itemId).getItemProperty(propertyURI).setValue(((BasicResultItem)itemId).getValue(propertyURI));
		}
		final boolean isDataProperty = UserSession.getManager().isDataProperty(propertyURI);
//		if(!UserSession.getManager().isDataProperty(propertyURI)){
			resultTable.addGeneratedColumn(propertyURI, new Table.ColumnGenerator() {
	            public Component generateCell(Table source, Object itemId,
	                    Object columnId) {
	            	String html = "";
	                BasicResultItem item = (BasicResultItem)itemId;
	                Set<Object> dataValues = (Set<Object>) item.getData().get(propertyURI);
	                
	                if(dataValues != null){
	                	for(Object value : dataValues){
	                		if(isDataProperty){
	                			html += value.toString();
	                		} else {
	                			String uri = (String) value;
				                html += "<a href=\"" + uri + "\" target=\"_blank\">" + Labels.getLabelForResource(uri) + "</a>";
	                		}
	                		html += "<br>";
		                }
	                	Label content = new Label(html, Label.CONTENT_XHTML);
	                	content.setHeight("180px");
	                	return content;
	                }
	                return null;
	                
	                
	            }

	        });
			resultTable.setColumnWidth(propertyURI, -1);
//		}
		
		resultTable.setColumnHeader(propertyURI, Labels.getLabel(propertyURI) + (isDataProperty  ? " \u2599" : ""));
		visibleColumns.add(propertyURI);
		
		resultTable.setVisibleColumns(visibleColumns.toArray());
		if(propertySelector != null){
			propertySelector.removeItem(propertyURI);
			propertySelector.setValue(null);
		}
		
	}
	
	@Override
	public void activate(View view) {
		mainPanel.replaceComponent((Component) currentView, (Component) view);
		currentView = view;
		mainPanel.setExpandRatio((Component) currentView, 1f);
	}

	@Override
	public void deactivate(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void message(String message) {
		feedbackLabel.setValue("<div>" + message + "</div>");
	}

	@Override
	public void finished(Answer answer) {
		this.answer = answer;
		executing = false;
	}
	
	private void showTable(List<BasicResultItem> result, List<String> prominentProperties, Map<String, Integer> additionalProperties){
		resultTable = new Table();
		resultTable.setSizeFull();
		resultTable.setHeight("100%");
		resultTable.setImmediate(true);
		resultTable.setColumnCollapsingAllowed(true);
		resultTable.setColumnReorderingAllowed(true);
		resultTable.addContainerProperty("uri", Label.class, null);
		resultTable.addContainerProperty("label", String.class, null);
		resultTable.addContainerProperty("description", String.class, null);
		resultTable.addContainerProperty("image", String.class, null);
		
		Item tabItem;
		for (BasicResultItem item : result) {
//			resultTable.addItem(
//					new Object[] { item.getUri(), item.getLabel(), item.getDescription(),
//							item.getImageURL() }, item);
			tabItem = resultTable.addItem(item);
			tabItem.getItemProperty("uri").setValue(item.getUri());
			tabItem.getItemProperty("label").setValue(item.getLabel());
			tabItem.getItemProperty("description").setValue(item.getDescription());
			tabItem.getItemProperty("image").setValue(item.getImageURL());
			if(item.getData() != null){
				for(Entry<String, Object> entry : item.getData().entrySet()){
					if(entry.getValue() != null){
						resultTable.addContainerProperty(entry.getKey(), entry.getValue().getClass(), null);
						tabItem.getItemProperty(entry.getKey()).setValue(entry.getValue());
					}
				}
			}
		}
		if(additionalProperties.isEmpty()){
			resultTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		}
		
		positiveMarkedRows = new ArrayList<Object>();
		negativeMarkedRows = new ArrayList<Object>();
		
		
		final CellStyleGenerator styleGen = new Table.CellStyleGenerator(){
			public String getStyle(Object itemId, Object propertyId) {

                for (Object row : positiveMarkedRows) {
                    if (row.equals(itemId)) {
                        return "green";
                    }    
                }
                for (Object row : negativeMarkedRows) {
                    if (row.equals(itemId)) {
                        return "red";
                    }    
                }
                return null;
            }
		};
		resultTable.setCellStyleGenerator(styleGen);

		resultTable.addGeneratedColumn("object", new ColumnGenerator() {
			@Override
			public Component generateCell(Table source, final Object itemId, Object columnId) {
				BasicResultItem item = (BasicResultItem) itemId;
				ExtendedKnowledgebase ekb = UserSession.getManager()
						.getCurrentExtendedKnowledgebase();
				
				HorizontalLayout c = null;
				try {
					c = (HorizontalLayout) ekb.getInfoBoxClass().getConstructor(BasicResultItem.class)
							.newInstance(item);
					((InfoLabel)c).addFeedBackListener(new FeedBackListener() {
						
						@Override
						public void positiveExampleSelected(BasicResultItem item) {
							if(positiveMarkedRows.contains(item)){
								positiveMarkedRows.remove(item);
							} else {
								negativeMarkedRows.remove(item);
								positiveMarkedRows.add(itemId);
							}
							resultTable.setCellStyleGenerator(styleGen);
							enableRefinement(positiveMarkedRows.size() >= 3);
							
							
						}
						
						@Override
						public void negativeExampleSelected(BasicResultItem item) {
							if(negativeMarkedRows.contains(item)){
								negativeMarkedRows.remove(item);
							} else {
								positiveMarkedRows.remove(item);
								negativeMarkedRows.add(itemId);
							}
							resultTable.setCellStyleGenerator(styleGen);
						}
					});
					
					c.setHeight("180px");
					c.setWidth(null);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
				return c;
			}
		});
		
		List<Object> visibleColumns = new ArrayList<Object>();
		visibleColumns.add("object");
//		
		resultTable.setColumnHeader("object", "");
		resultTable.setVisibleColumns(visibleColumns.toArray());
		
		resultTable.setColumnWidth("object", -1);
		
		for(String prominentProperty : prominentProperties){
			onAddTableColumn(prominentProperty);
		}
		
		PortalLayout pl = new PortalLayout();
		pl.addComponent(resultTable);
		pl.setClosable(resultTable, false);
		pl.setCollapsible(resultTable, false);
		pl.setSizeFull();
		
		HorizontalLayout header = new HorizontalLayout();
		header.setSizeUndefined();
        header.setSpacing(true);
        pl.setHeaderComponent(resultTable, header);
		
        List<String> existingProperties = new ArrayList<String>(prominentProperties);
        existingProperties.addAll(additionalProperties.keySet());
        if(canShowMap(existingProperties)){
        	NativeButton showMapButton = new NativeButton();
    		showMapButton.addStyleName("borderless");
    		showMapButton.addStyleName("map-button");
    		Resource icon = new ThemeResource("images/map2.png");
//    		showMapButton.setIcon(icon);
    		showMapButton.setDescription("Show in map.");
    		showMapButton.setHeight("100%");
    		showMapButton.addListener(new Button.ClickListener() {
    			
    			@Override
    			public void buttonClick(ClickEvent event) {
    				onShowMap();
    			}
    		});
    		header.addComponent(showMapButton);
        }
		
		if(!additionalProperties.isEmpty()){
			Label l = new Label("Show also ");
			l.setHeight("100%");
			l.addStyleName("white-font");
			propertySelector = new ComboBox();
			final IndexedContainer ic = new IndexedContainer();
			ic.addContainerProperty("label", String.class, null);
			for(Entry<String, Integer> entry : additionalProperties.entrySet()){
				String propertyURI = entry.getKey();
				ic.addItem(propertyURI).getItemProperty("label").setValue(Labels.getLabel(propertyURI));
			}
			propertySelector.setContainerDataSource(ic);
			propertySelector.setWidth("200px");
			propertySelector.setItemCaptionPropertyId("label");
			propertySelector.setFilteringMode(Filtering.FILTERINGMODE_STARTSWITH);
			propertySelector.setImmediate(true);
			propertySelector.addListener(new Property.ValueChangeListener() {
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					String propertyURI = event.getProperty().toString();
					if(propertyURI != null){
						UserSession.getManager().fillItems(propertyURI);
						onAddTableColumn(propertyURI);
					}
				}
			});

			header.addComponent(l);
	        header.addComponent(propertySelector);
	        header.setSpacing(true);
	        header.setComponentAlignment(propertySelector, Alignment.MIDDLE_LEFT);
		} else if(UserSession.getManager().getKnowledgebases().indexOf(UserSession.getManager().getCurrentExtendedKnowledgebase()) == 0){
			Label l = new Label("Sort by ");
			l.setHeight("100%");
			l.addStyleName("white-font");
			
			NativeSelect sortSelector = new NativeSelect();
			sortSelector.setImmediate(true);
			sortSelector.addContainerProperty("label", String.class, null);
			sortSelector.setItemCaptionPropertyId("label");
			for(SortProperty sort : SortProperty.values()){
				sortSelector.addItem(sort).getItemProperty("label").setValue(sort.getLabel());
			}
			sortSelector.addListener(new Property.ValueChangeListener() {
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					SortProperty sortProp = (SortProperty) event.getProperty().getValue();
					resultTable.sort(new Object[]{sortProp.getId()}, new boolean[]{sortProp.isAscending()});
				}
			});
			
			NativeButton showDiagramButton = new NativeButton();
			showDiagramButton.addStyleName("borderless");
			showDiagramButton.setHeight("100%");
			showDiagramButton.addStyleName("diagram-button");
//			showDiagramButton.setIcon(new ThemeResource("images/diagram.png"));
			showDiagramButton.setDescription("Visualize price.");
			showDiagramButton.addListener(new Button.ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					onShowChart("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice");
				}
			});

			header.addComponent(showDiagramButton);
			header.addComponent(l);
	        header.addComponent(sortSelector);
	        header.setSpacing(true);
	        header.setComponentAlignment(sortSelector, Alignment.MIDDLE_LEFT);
		}
		
		resultTable.addListener(new Table.HeaderClickListener() {
            private static final long serialVersionUID = 2927158541717666732L;

            public void headerClick(HeaderClickEvent event) {
                String column = (String) event.getPropertyId();System.out.println(column);
                onShowChart(column);
            }
        });
		
		resultHolderPanel.addComponent(pl);
	}
	
	private boolean canShowMap(List<String> existingProperties){
		return 	UserSession.getManager().getKnowledgebases().indexOf(UserSession.getManager().getCurrentExtendedKnowledgebase()) == 0 
				||
				(existingProperties.contains("http://www.w3.org/2003/01/geo/wgs84_pos#lat") && existingProperties.contains("http://www.w3.org/2003/01/geo/wgs84_pos#long"));
	}
	
	private void onShowMap(){
		String queryString = UserSession.getManager().getLearnedSPARQLQuery();
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		query = JENAUtils.writeOutPrefixes(query);
		String targetVar = query.getProjectVars().get(0).getVarName();
		String serviceURI = UserSession.getManager().getCurrentExtendedKnowledgebase().getKnowledgebase().getEndpoint().getURL().toString();
		String url = "";
		try {
			url = Manager.getInstance().getSemMapURL() 
					+ "?query=" + URLEncoder.encode(query.toString().replaceAll("[\n\r]", " "), "UTF-8")
					+ "&var=" + targetVar
					+ "&service-uri=" + EscapeUtils.encodeURIComponent(serviceURI);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println(url);
		Embedded e = new Embedded("Linked Geo Data View", new ExternalResource(url));
        e.setAlternateText("Linked Geo Data View");
        e.setType(Embedded.TYPE_BROWSER);
        e.setSizeFull();
        
        final Window w = new Window();
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        w.setContent(mainLayout);
        w.setHeight("800px");
        w.setWidth("800px");
        mainLayout.addComponent(e);
        w.addListener(new Window.CloseListener() {

			@Override
			public void windowClose(CloseEvent e) {
				MainView.this.getApplication().getMainWindow().removeWindow(w);
			}
		});
		getApplication().getMainWindow().addWindow(w);
        
	}
	
	private void onShowChart(String propertyURI) {
		Map<String, Set<Object>> data = UserSession.getManager().getDataForProperty(propertyURI);
		XSDDatatype datatype = UserSession.getManager().getDatatype(propertyURI);
		Map<String, String> uri2Label = new HashMap<String, String>();
		for(Entry<String, BasicResultItem> entry : UserSession.getManager().getUri2Items().entrySet()){
			uri2Label.put(entry.getKey(), entry.getValue().getLabel());
		}
		final Window w;
		if(propertyURI.equals("http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice")){
			 w = new OxfordPriceChartWindow(UserSession.getManager().getCurrentQuestion(),
						propertyURI, uri2Label, data);
		} else {
			w = Charts.getChart(UserSession.getManager().getCurrentQuestion(),
					propertyURI, datatype, uri2Label, data);
		}
		
		if(w != null){
			w.addListener(new Window.CloseListener() {

				@Override
				public void windowClose(CloseEvent e) {
					MainView.this.getApplication().getMainWindow().removeWindow(w);
				}
			});
			getApplication().getMainWindow().addWindow(w);
		}
	

	}
	
	private void showAnswer(Answer answer){
		if (!answer.isBoolean()) {
			SelectAnswer sAnswer = (SelectAnswer) answer;
			List<BasicResultItem> result = sAnswer.getItems();
			List<String> prominentProperties = sAnswer.getProminentProperties();
			Map<String, Integer> additionalProperties = sAnswer.getAdditionalProperties();
			// show the result in a table
			showTable(result, prominentProperties, additionalProperties);
			
		} else {
			//TODO show boolean answer
		}
	}
	
	private void enableRefinement(boolean enabled){
		refineButton.setVisible(enabled);
	}
	
	private void onRefineResult(){
		List<String> posExamples = new ArrayList<String>();
		for(Object row : positiveMarkedRows){
			posExamples.add(((BasicResultItem)row).getUri());
		}
		List<String> negExamples = new ArrayList<String>();
		for(Object row : negativeMarkedRows){
			negExamples.add(((BasicResultItem)row).getUri());
		}
		Refinement refinement = UserSession.getManager().refine(posExamples, negExamples);
		if(refinement != null){
			resultHolderPanel.removeAllComponents();
			showAnswer(refinement.getRefinedAnswer());
		} else {
			//TODO show message that refinement failed
		}
	}

	@Override
	public void foundAnswer(boolean answerFound) {
		if(answerFound){
//			wrongSolutionButton.setVisible(true);
			feedbackPanel.addComponent(wrongSolutionButton);
			feedbackPanel.setComponentAlignment(wrongSolutionButton, Alignment.MIDDLE_CENTER);
		}
		
	}
	
	private void showFeedback(boolean show){
		feedbackPanel.setVisible(show);
	}
	
	private void resetFeedback(){
		feedbackLabel.setValue("&nbsp;");
		feedbackPanel.removeComponent(wrongSolutionButton);
	}
	
	

}
