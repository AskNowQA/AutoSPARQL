package org.autosparql.client.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

public class SearchResultPanel extends ContentPanel {

	public ListStore<Example> gridStore;
	public Grid<Example> grid;
	private PagingLoader<PagingLoadResult<Example>> loader;
	private PagingModelMemoryProxy proxy;
	
	private LinkedList<Example> positives = new LinkedList<Example>();
	private LinkedList<Example> negatives = new LinkedList<Example>();	
	
	private Button relearnButton = new Button("relearn");
	
	private SearchResultPanelSelectionListener listener = new SearchResultPanelSelectionListener();

	class SearchResultPanelSelectionListener extends SelectionListener<ButtonEvent>
	{
		@Override
		public void componentSelected(ButtonEvent ce)
		{
			if(ce.getSource()==relearnButton)
			{
				
			}
		}		
	}
	
	public SearchResultPanel()
	{
		setHeading("Result");
		setCollapsible(false);
		//setLayout(new FitLayout());
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		//setBottomComponent(toolbar);
		
		grid = createExampleGrid();
		grid.setHeight(500);
		add(grid);
		final PagingToolBar toolbar = new PagingToolBar(5);
		setTopComponent(toolbar);

		toolbar.bind(loader);
		relearnButton.addSelectionListener(listener);
		add(relearnButton );
		
		setBottomComponent(new Button("bottom"));
		//grid.setAutoHeight(true);
	}

	public void markPositive(Example e)
	{
		positives.add(e);
	}

	public void markNegative(Example e)
	{
		negatives.add(e);
		gridStore.remove(e);
		//Window.alert("removing "+e);
	}

	private Grid<Example> createExampleGrid()
	{
//				proxy = new PagingModelMemoryProxy(null);
//				loader = new BasePagingLoader<PagingLoadResult<Example>>(proxy);
//				loader.setRemoteSort(true);
//				gridStore = new ListStore<Example>(loader);
//				
//				ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
//				
//				XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a>");
//				RowExpander expander = new RowExpander();
//				expander.setTemplate(tpl);
//				columns.add(expander);
//				
//				GridCellRenderer<Example> imageRender = new ImageCellRenderer();
//				
//				ColumnConfig c = new ColumnConfig();
//				c.setId("imageURL");
//				columns.add(c);
//				c.setWidth(50);
//				c.setRenderer(imageRender);
//				
//				c = new ColumnConfig();
//				c.setId("label");
//				columns.add(c);
//				
//				GridCellRenderer<Example> buttonRender = new GridCellRenderer<Example>() {
//		
//					@Override
//					public Object render(final Example model, String property,
//							ColumnData config, int rowIndex, int colIndex,
//							ListStore<Example> store, Grid<Example> grid) {
//						//ContentPanel p = new ContentPanel();
//						//VerticalPanel p = new VerticalPanel();
//						FlowPanel p = new FlowPanel();
//						//p.setLayoutData(null);
//						//p.add(new Button("+"));
//						//p.add(new Button("-"));
//						//p.add(new Button("test3"));
//						
//						//p.set
//						//p.setLayout(new RowLayout(Orientation.VERTICAL));
//						//VerticalPanel p = new VerticalPanel();
//						//p.getLa
//						
//						//p.setBorders(true);
//						//p.setSize(100, 100);
//						Button addPosButton = new Button("+");
//		                                addPosButton.addStyleName("button-positive");
//						addPosButton.setSize(40, 40);
//						
//						addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//							@Override
//							public void componentSelected(ButtonEvent ce) {
//								
//							}
//						});
//						Button addNegButton = new Button("&ndash;");
//		                                addNegButton.addStyleName("button-negative");
//						addNegButton.setSize(40, 40);
//						addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//							@Override
//							public void componentSelected(ButtonEvent ce) {
//								
//							}
//						});
//						//p.setVerticalAlign(VerticalAlignment.MIDDLE);
//						p.add(addPosButton);
//						p.add(addNegButton);
//						
//						return p;
//					}
//				
//				};
//				
//				c = new ColumnConfig();
//				c.setId("");
//				c.setWidth(50);
//				c.setRenderer(buttonRender);
//				columns.add(c);
//		
//				ColumnModel cm = new ColumnModel(columns);
//				
//				Grid<Example> grid = new Grid<Example>(gridStore, cm);
//				grid.setHideHeaders(true);
//				grid.setAutoExpandColumn("label");
//				grid.setLoadMask(true);
//				grid.addPlugin(expander);
//				grid.getView().setEmptyText("");
//				
//				return grid;

		//	Example leipzig = new Example("dbpedia.org/Leipzig", "Leipzig", "leipzig.png", "a beautiful city.");


				ArrayList<Example> examples = new ArrayList<Example>();
				for(int i=0;i<10;i++)
				{
					Example leipzig = new Example("dbpedia.org/Leipzig"+i, "Leipzig"+i, "leipzig.png", "a beautiful city.");
					examples.add(leipzig);
				}
				
				List<ColumnConfig> configs = new LinkedList<ColumnConfig>();
				
				ColumnConfig buttonConfig = new ColumnConfig("button", "", 30);
				buttonConfig.setRenderer(new PlusMinusButtonCellRender(this));
				configs.add(buttonConfig);

				//configs.add(new ColumnConfig("uri", "url", 200));
				ColumnConfig labelConfig = new ColumnConfig("label", "label", 100);
				labelConfig.setResizable(true);
				configs.add(labelConfig);
				
				ColumnConfig imageConfig = new ColumnConfig("imageURL", "imageURL", 100);
				imageConfig.setRenderer(new ImageCellRenderer(100,100));
				configs.add(imageConfig);
				
				ColumnConfig commentConfig = new ColumnConfig("comment", "comment", 100);
//				commentConfig.addS
				configs.add(commentConfig);

				
				proxy = new PagingModelMemoryProxy(examples);//
//				BasePagingLoadConfig config = new BasePagingLoadConfig(0, 5);
		
				//		Window.alert(proxy.getData().toString());
				//		proxy.setData(examples);
		
				//DataProxy<Example> proxy = new MemoryProxy<Example>(examples);
		
				loader = new BasePagingLoader<PagingLoadResult<Example>>(proxy);
			
				gridStore = new ListStore<Example>(loader);

				//store.add();
				ColumnModel cm = new ColumnModel(configs);
				grid = new Grid<Example>(gridStore,cm);
				

//				GridView view = grid.getView();
//				view.setAutoFill(true);
//				view.setForceFit(true);
				//grid.set
				
//				grid.setAutoHeight(true);
//				grid.setAutoWidth(true);
				grid.setColumnResize(true);
				grid.setColumnLines(true);
				grid.setColumnReordering(true);
				//grid.setStripeRows(true);
						
//				grid.setAutoExpandColumn("uri");
//				grid.setAutoExpandColumn("label");
//				grid.setAutoExpandColumn("imageURL");
				grid.setAutoExpandColumn("comment");
				//loader.load(config);

return grid;
	}

	public void setResult(List<Example> result){
		//Window.alert(result.toString());
		proxy.setData(result);
		loader.load();
		layout();
	}

}
