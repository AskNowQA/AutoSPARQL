package org.autosparql.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.autosparql.client.TestData;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.RowExpander;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.ui.FlowPanel;

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
		
		setResult(TestData.getDummyExamples());
	}
	
	private Grid<Example> createExampleGrid(){
		proxy = new PagingModelMemoryProxy(null);
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy);
		loader.setRemoteSort(true);
		
		gridStore = new ListStore<Example>(loader);
		
		ArrayList<ColumnConfig> columns = new ArrayList<ColumnConfig>();
		
		XTemplate tpl = XTemplate.create("<p><b>Comment:</b><br>{comment}</p><p><a href = \"{uri}\" target=\"_blank\"/>Link to resource page</a>");
		RowExpander expander = new RowExpander();
		expander.setTemplate(tpl);
		columns.add(expander);
		
		GridCellRenderer<Example> imageRender = new ImageCellRenderer();
		
		ColumnConfig c = new ColumnConfig();
		c.setId("imageURL");
		columns.add(c);
		c.setWidth(50);
		c.setRenderer(imageRender);
		
		c = new ColumnConfig();
		c.setId("label");
		columns.add(c);
		
		GridCellRenderer<Example> buttonRender = new GridCellRenderer<Example>() {

			@Override
			public Object render(final Example model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<Example> store, Grid<Example> grid) {
				//ContentPanel p = new ContentPanel();
				//VerticalPanel p = new VerticalPanel();
				FlowPanel p = new FlowPanel();
				//p.setLayoutData(null);
				//p.add(new Button("+"));
				//p.add(new Button("-"));
				//p.add(new Button("test3"));
				
				//p.set
				//p.setLayout(new RowLayout(Orientation.VERTICAL));
				//VerticalPanel p = new VerticalPanel();
				//p.getLa
				
				//p.setBorders(true);
				//p.setSize(100, 100);
				Button addPosButton = new Button("+");
                                addPosButton.addStyleName("button-positive");
				addPosButton.setSize(40, 40);
				
				addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						
					}
				});
				Button addNegButton = new Button("&ndash;");
                                addNegButton.addStyleName("button-negative");
				addNegButton.setSize(40, 40);
				addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						
					}
				});
				//p.setVerticalAlign(VerticalAlignment.MIDDLE);
				p.add(addPosButton);
				p.add(addNegButton);
				
				return p;
			}
		
		};
		
		c = new ColumnConfig();
		c.setId("");
		c.setWidth(50);
		c.setRenderer(buttonRender);
		columns.add(c);
		
		ColumnModel cm = new ColumnModel(columns);
		
		Grid<Example> grid = new Grid<Example>(gridStore, cm);
		grid.setHideHeaders(true);
		grid.setAutoExpandColumn("label");
		grid.setLoadMask(true);
		grid.addPlugin(expander);
		grid.getView().setEmptyText("");
		
		return grid;
	}
	
	public void setResult(List<Example> result){
		proxy.setData(result);
		loader.load();
	}

}
