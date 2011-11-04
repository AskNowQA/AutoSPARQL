package org.autosparql.client.widget;

import org.autosparql.client.ImageBundle;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class PlusMinusButtonCellRender implements GridCellRenderer<Example>
{
	final SearchResultPanel panel;
	
	public PlusMinusButtonCellRender(SearchResultPanel panel)
	{
		super();
		this.panel = panel;
	}
		
	@Override
	public Object render(final Example example, String property,
			ColumnData config, int rowIndex, int colIndex,
			final ListStore<Example> store, Grid<Example> grid) {
		
		LayoutContainer p = new LayoutContainer(new RowLayout(Orientation.VERTICAL));
	
		p.setLayoutData(new FitLayout());
		//p.setSize("2.5em", "4.2em");
		Button[] buttons = new Button[2];		
		for(int i=0;i<buttons.length;i++)
		{
			final Button button = (buttons[i] = new Button());
			button.setSize("35px","35px");
			
			p.add(button,new RowData(-1,-1,new Margins(5)));
			button.setIcon(AbstractImagePrototype.create(i==0?ImageBundle.INSTANCE.yes():ImageBundle.INSTANCE.no()));
			//button.setIconStyle("img/icon-yes.svg");
			final int iCopy = i;
			button.addSelectionListener(new SelectionListener<ButtonEvent>() {
				@Override
				public void componentSelected(ButtonEvent ce)
				{
					if(iCopy==0)
						{
							button.setEnabled(false);
							panel.markPositive(example);
						}
					else panel.markNegative(example);
				}
			});
		}
//		Button addPosButton = new Button("+");
//        addPosButton.addStyleName("button-positive");
//		addPosButton.setSize(20, 20);
//		addPosButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//			@Override
//			public void componentSelected(ButtonEvent ce) {
//				AppEvent event = new AppEvent(AppEvents.AddExample);
//				event.setData("example", model);
//				event.setData("type", Example.Type.POSITIVE);
//				Dispatcher.forwardEvent(event);
////				store.remove(model);
//			}
//		});
//		Button addNegButton = new Button("&ndash;");
//                        addNegButton.addStyleName("button-negative");
//		addNegButton.setSize(20, 20);
//		addNegButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
//			@Override
//			public void componentSelected(ButtonEvent ce) {
//				AppEvent event = new AppEvent(AppEvents.AddExample);
//				event.setData("example", model);
//				event.setData("type", Example.Type.NEGATIVE);
//				Dispatcher.forwardEvent(event);
////				store.remove(model);
//			}
//		});
//		p.add(addPosButton);
//		p.add(addNegButton);
		return p;
	}


}
