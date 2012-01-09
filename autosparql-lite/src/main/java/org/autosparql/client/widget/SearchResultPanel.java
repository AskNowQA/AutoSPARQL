package org.autosparql.client.widget;

import static org.autosparql.shared.StringUtils.abbreviate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.autosparql.client.AutoSPARQLService;
import org.autosparql.client.AutoSPARQLServiceAsync;
import org.autosparql.client.Transformer;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.FastSet;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.BufferView;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SearchResultPanel extends ContentPanel
{
	AutoSPARQLServiceAsync service = AutoSPARQLService.Util.getInstance();
	private static final Set<String> defaultProperties = defaultProperties();
	private static final Logger log = Logger.getLogger(SearchResultPanel.class.toString());

	private static final Set<String> imageProperties = new HashSet<String>(Arrays.asList(new String[]
			{
			"http://dbpedia.org/ontology/thumbnail",
			"http://xmlns.com/foaf/0.1/depiction",
			"http://dbpedia.org/property/wappen"
			}));

	private static Set<String> defaultProperties()
	{
		Set<String> defaultProperties = new FastSet();
		for(String s : new String[] {"http://www.w3.org/2000/01/rdf-schema#label","imageURL","http://www.w3.org/2000/01/rdf-schema#comment","uri"}) {defaultProperties.add(s);}
		return defaultProperties;
	}

	private static final boolean HIGHLIGHT_POSITIVES_AND_NEGATIVES = true;
	private static final double MIN_OCCURRENCE = 0.7;
	private static final int NUMBER_OF_POSITIVES_FOR_RELEARN = 3;

	public Grid<Example> grid = null;
	ListStore<Example> gridStore = null;

	private final FastSet positives = new FastSet();
	private final FastSet negatives = new FastSet();

	private final FastSet newPositives = new FastSet();
	private final FastSet newNegatives = new FastSet();

	private final PagingToolBar toolbar;
	//private Button relearnButton = new Button("relearn");
	//private int newPositives = 0;

	private SearchResultPanelSelectionListener listener = new SearchResultPanelSelectionListener();

	class SearchResultPanelSelectionListener extends SelectionListener<ButtonEvent>
	{
		@Override
		public void componentSelected(ButtonEvent ce)
		{
			//			if(ce.getSource()==relearnButton)
			//			{
			//				relearn();
			//			}
		}		
	}


	public SearchResultPanel()
	{
		setHeading("Result");
		setCollapsible(false);
		//setLayout(new FitLayout());
		setLayout(new RowLayout(Orientation.VERTICAL));

		//setBottomComponent(toolbar);

		//		relearnButton.addSelectionListener(listener);
		//		addButton(relearnButton );


		toolbar = new PagingToolBar(10);
		Button relearnButton = new Button("Relearn");
		toolbar.add(relearnButton);
		relearnButton.addSelectionListener(new SelectionListener<ButtonEvent>()
		{ @Override public void componentSelected(ButtonEvent ce) {learn();}});

		setBottomComponent(toolbar);

		//		setTopComponent(toolbar);

		//grid.setAutoHeight(true);
	}

	public void markPositive(Example e, int rowIndex)
	{
		if(!newPositives.contains(e.getURI()))
		{
			newNegatives.remove(e.getURI());
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Negative");
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Even");
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Odd");
			grid.getView().getRow(rowIndex).addClassName("row-Style-Positive");
			if(!positives.contains(e.getURI()))
			{
				newPositives.add(e.getURI());
				//log.info("Added positive example <"+e.getURI()+">. "+newPositives.size());
				if(newPositives.size()>=NUMBER_OF_POSITIVES_FOR_RELEARN) {log.info("Relearning"); learn();}
			}
		}
	}

	public void markNegative(Example e, int rowIndex)
	{
		if(!newNegatives.contains(e.getURI()))
		{
			newPositives.remove(e.getURI());
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Positive");
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Even");
			grid.getView().getRow(rowIndex).removeClassName("row-Style-Odd");
			grid.getView().getRow(rowIndex).addClassName("row-Style-Negative");
			if(!negatives.contains(e.getURI()))
			{
				newNegatives.add(e.getURI());
			}
		}
		//if(gridStore!=null) {gridStore.remove(e);}
	}

	private List<ColumnConfig> columnConfigs(List<Example> examples)
	{
		log.info("Creating column configs");
		List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();

		ColumnConfig buttonConfig = new ColumnConfig("button", "", 55);
		buttonConfig.setAlignment(HorizontalAlignment.LEFT);
		buttonConfig.setRenderer(new PlusMinusButtonCellRender(this));
		columnConfigs.add(buttonConfig);

		//configs.add(new ColumnConfig("uri", "url", 200));
		ColumnConfig labelConfig = new ColumnConfig("http://www.w3.org/2000/01/rdf-schema#label", "label", 200);
		labelConfig.setRenderer(new LabelRenderer());
		//labelConfig.setResizable(true);
		columnConfigs.add(labelConfig);

		ColumnConfig imageConfig = new ColumnConfig("http://xmlns.com/foaf/0.1/depiction", "image", 100);
		imageConfig.setRenderer(new ImageCellRenderer(imageProperties,100,100));
		columnConfigs.add(imageConfig);

		ColumnConfig commentConfig = new ColumnConfig("http://www.w3.org/2000/01/rdf-schema#comment", "comment", 300);
		commentConfig.setRenderer(new CommentRenderer());
		columnConfigs.add(commentConfig);

		SortedSet<String> properties = new TreeSet<String>();
		for(Example example: examples)
		{	
			properties.addAll(example.getProperties().keySet());
		}

		// Remove rare properties ******************************************
		Map<String,Integer> propertyCounts = new HashMap<String,Integer>();
		for(String property: properties)
		{
			propertyCounts.put(property,0);
		}
		for(Example example: examples)
		{
			for(String property: example.getPropertyNames())
			{
				String object = example.get(property);
				if(object!=null&&!object.isEmpty()) {propertyCounts.put(property,propertyCounts.get(property)+1);}
			}
		}
		// remove all properties with occurrence < 0.5
		Set<String> initialProperties = new FastSet();

		for(String property : properties)
		{
			if(propertyCounts.get(property)>=examples.size()*MIN_OCCURRENCE) {initialProperties.add(property);}
		}
		// End Remove rare properties ******************************************
		log.info("Shrinked initial properties from "+properties.size()+" to "+initialProperties.size()+".");
		// Sort properties by their alphabetically ascending by their displayed form
		Map<String,String> transformedToOriginalProperty = new HashMap<String,String>();
		for(String property: properties) {transformedToOriginalProperty.put(Transformer.displayProperty(property),property);}
		List<String> transformedProperties = new ArrayList<String>(transformedToOriginalProperty.keySet());
		Collections.sort(transformedProperties);
		for(String transformedProperty: transformedProperties)
		{
			String property = transformedToOriginalProperty.get(transformedProperty);
			if(defaultProperties.contains(property)) {continue;}
			if(imageProperties.contains(property)) {continue;}
			ColumnConfig config = new ColumnConfig(property,Transformer.displayProperty(property),150);
			config.setRenderer(new LiteralRenderer());
			config.setHidden(!initialProperties.contains(property));
			//if(property.contains("image")) {config.setRenderer(new ImageCellRenderer(100,100));}

			columnConfigs.add(config);
		}

		return columnConfigs;
	}

	private Grid<Example> createGrid(List<Example> examples)
	{
		//LinkedList<Example> examples = new LinkedList<Example>(examples1);
		//for(Example example: examples) if(!example.getURI().equals("http://dbpedia.org/resource/Digital_Fortress")) examples.remove(example);
		log.info("Creating grid with examples: "+abbreviate(Example.getURIs(examples).toString(),100));
		PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(examples);
		PagingLoader<PagingLoadResult<Example>> loader = new BasePagingLoader<PagingLoadResult<Example>>(proxy);
		toolbar.bind(loader);	
		gridStore = new ListStore<Example>(loader);
		grid = new Grid<Example>(gridStore,new ColumnModel(columnConfigs(examples)));
		//grid.setWidth("100%");
		grid.setAutoWidth(false);
		grid.setHeight(1050);
		//grid.setAutoHeight(true);

		grid.getView().setAutoFill(true);

		BufferView view = new BufferView();
		//		view.ensureVisible(4, 0, false);
		view.setRowHeight(100);
		//		//view.setForceFit(true);
		grid.setView(view);
		loader.load();

		//		GridView view = grid.getView();
		//		view.setAutoFill(true);
		//		//view.setForceFit(true);
		//		grid.setColumnLines(true);
		//		//grid.setColumnReordering(true);
		//grid.setAutoExpandColumn("http://www.w3.org/2000/01/rdf-schema#comment");
		//grid.setAutoHeight(true);

		//		grid.setAutoWidth(true);
		//		grid.setColumnResize(true);
		//		grid.setColumnLines(true);
		//		grid.setColumnReordering(true);
		//		//grid.setStripeRows(true);
		//		//grid.setAutoExpandColumn("uri");
		//		//grid.setAutoExpandColumn("label");
		//		//grid.setAutoExpandColumn("imageURL");
		updateRowStyle();

		return grid;
	}

	private void updateRowStyle()
	{
		if(grid!=null)
		{
			grid.getView().setViewConfig(new GridViewConfig(){
				@Override
				public String getRowStyle(ModelData model, int rowIndex,ListStore<ModelData> ds)
				{
					//Window.alert(model.get("uri").toString());
					if(HIGHLIGHT_POSITIVES_AND_NEGATIVES&&positives.contains(model.get("uri")))	{return "row-Style-Positive";}
					else if(HIGHLIGHT_POSITIVES_AND_NEGATIVES&&negatives.contains(model.get("uri")))	{return "row-Style-Negative";}
					else if(model instanceof Example&&((Example)model).containsSolrData) {return "row-Style-SolrData";}
					else if(rowIndex % 2 == 0)		{return "row-Style-Odd";}	
					return "row-Style-Even";
				}
			});
		}
	}

	public void setResult(List<Example> examples)
	{
		if(grid!=null) {this.remove(grid);}
		grid = createGrid(examples);

		add(grid);
		layout();
	}

	public void learn()
	{
		positives.addAll(newPositives);
		positives.removeAll(newNegatives);
		negatives.addAll(newNegatives);
		negatives.removeAll(newPositives);
		newPositives.clear();
		newNegatives.clear();
		log.info("Learning...");

		final WaitDialog waiting  = new WaitDialog("Updating the table");
		waiting.show();
		AsyncCallback<SortedSet<Example>> callback = new AsyncCallback<SortedSet<Example>>()
				{
			@Override
			public void onSuccess(SortedSet<Example> examples)
			{
				log.info("Learning successfull, found "+examples.size()+" examples.");
				//if(1==1)throw new RuntimeException(examples.toString());
				
				setResult(new LinkedList<Example>(examples));
				waiting.hide();
			}

			@Override
			public void onFailure(Throwable caught)
			{
				log.info("Learning failed: "+caught.getLocalizedMessage());
				waiting.hide();
				throw new RuntimeException(caught);				
			}
				};

				service.getExamplesByQTL(new ArrayList<String>(positives), new ArrayList<String>(negatives), callback);
	}

}