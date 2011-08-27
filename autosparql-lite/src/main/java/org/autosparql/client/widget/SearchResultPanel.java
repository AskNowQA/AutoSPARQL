package org.autosparql.client.widget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;

public class SearchResultPanel extends ContentPanel {
	
	public ListStore<Example> gridStore;
	
	private PagingLoader<PagingLoadResult<ModelData>> loader;
	private PagingModelMemoryProxy proxy;

	public SearchResultPanel() {
		setHeading("Result");
		setCollapsible(false);
		setLayout(new FitLayout());
		
		Grid<Example> grid = createExampleGrid();
		add(grid);
		
		
		final PagingToolBar toolbar = new PagingToolBar(10);
		toolbar.bind(loader);
		setBottomComponent(toolbar);
		
		//setResult(TestData.getDummyExamples());
	}
	
	private Grid<Example> createExampleGrid(){
//		proxy = new PagingModelMemoryProxy(null);
//		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
//		loader.setRemoteSort(true);
		
//		gridStore = new ListStore<Example>(loader);
//		
//		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
//		
//		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a>");
//		RowExpander expander = new RowExpander();
//		expander.setTemplate(tpl);
//		columns.add(expander);
//		
//		GridCellRenderer<Example> imageRender = new ImageCellRenderer();
//		
//		ColumnConfig c = new ColumnConfig();
//		c.setId("imageURL");
//		columns.add(c);
//		c.setWidth(50);
//		c.setRenderer(imageRender);
//		
//		c = new ColumnConfig();
//		c.setId("label");
//		columns.add(c);
//		
//		GridCellRenderer<Example> buttonRender = new GridCellRenderer<Example>() {
//
//			@Override
//			public Object render(final Example model, String property,
//					ColumnData config, int rowIndex, int colIndex,
//					ListStore<Example> store, Grid<Example> grid) {
//				//ContentPanel p = new ContentPanel();
//				//VerticalPanel p = new VerticalPanel();
//				FlowPanel p = new FlowPanel();
//				//p.setLayoutData(null);
//				//p.add(new Button("+"));
//				//p.add(new Button("-"));
//				//p.add(new Button("test3"));
//				
//				//p.set
//				//p.setLayout(new RowLayout(Orientation.VERTICAL));
//				//VerticalPanel p = new VerticalPanel();
//				//p.getLa
//				
//				//p.setBorders(true);
//				//p.setSize(100, 100);
//				Button addPosButton = new Button("+");
//                                addPosButton.addStyleName("button-positive");
//				addPosButton.setSize(40, 40);
//				
//				addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//					@Override
//					public void componentSelected(ButtonEvent ce) {
//						
//					}
//				});
//				Button addNegButton = new Button("&ndash;");
//                                addNegButton.addStyleName("button-negative");
//				addNegButton.setSize(40, 40);
//				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//					@Override
//					public void componentSelected(ButtonEvent ce) {
//						
//					}
//				});
//				//p.setVerticalAlign(VerticalAlignment.MIDDLE);
//				p.add(addPosButton);
//				p.add(addNegButton);
//				
//				return p;
//			}
//		
//		};
//		
//		c = new ColumnConfig();
//		c.setId("");
//		c.setWidth(50);
//		c.setRenderer(buttonRender);
//		columns.add(c);
//
//		ColumnModel cm = new ColumnModel(columns);
//		
//		Grid<Example> grid = new Grid<Example>(gridStore, cm);
//		grid.setHideHeaders(true);
//		grid.setAutoExpandColumn("label");
//		grid.setLoadMask(true);
//		grid.addPlugin(expander);
//		grid.getView().setEmptyText("");
//		
//		return grid;
		
		Example leipzig = new Example("dbpedia.org/Leipzig", "Leipzig", "leipzig.png", "a beautiful city.");
		
		ArrayList<Example> examples = new ArrayList<Example>();
		examples.add(leipzig);
		List<ColumnConfig> configs = new LinkedList<ColumnConfig>();
		configs.add(new ColumnConfig("uri", "url", 200));
		configs.add(new ColumnConfig("label", "label", 100));
		configs.add(new ColumnConfig("comment", "comment", 200));
		
		DataProxy<Example> proxy = new MemoryProxy<Example>(examples);
		
		BaseListLoader<BaseListLoadResult<Example>> loader = new BaseListLoader<BaseListLoadResult<Example>>(proxy);
		ListStore<Example> store = new ListStore<Example>(loader);
		//store.add();
		ColumnModel cm = new ColumnModel(configs);
		Grid<Example> grid = new Grid<Example>(store,cm);
		loader.load();
		return grid;
	}
	
	public void setResult(List<Example> result){
		proxy.setData(result);
		loader.load();
	}

}
