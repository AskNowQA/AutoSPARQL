package org.autosparql.client.controller;

import java.util.List;

import org.autosparql.client.AppEvents;
import org.autosparql.client.AutoSPARQLServiceAsync;
import org.autosparql.client.view.ApplicationView;
import org.autosparql.client.widget.ErrorDialog;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ApplicationController extends Controller
{
	AutoSPARQLServiceAsync service;
	private ApplicationView appView;

	public ApplicationController(AutoSPARQLServiceAsync service)
	{
		this.service = service;
		registerEventTypes(AppEvents.Init);
		registerEventTypes(AppEvents.Error);
	}

	public void handleEvent(AppEvent event)
	{
		EventType type = event.getType();
		if (type == AppEvents.Init) {
			onInit(event);
		} else if (type == AppEvents.Error) {
			onError((Throwable)event.getData());
		}
	}

	public void initialize()
	{
		String query = com.google.gwt.user.client.Window.Location.getParameter("query");
		if(query==null||query.isEmpty())
		{
			
		}
		else
		{
			service.getExamples(query, new AsyncCallback<List<Example>>() {				
				@Override
				public void onSuccess(List<Example> examples)
				{
					//new Example("testuri", "testlabel", "testimageurl", "testcomment");
					appView.display(examples);
				}
				
				@Override
				public void onFailure(Throwable arg0)
				{
					com.google.gwt.user.client.Window.alert(arg0.getMessage());
					//appView.showError("could not get examples");
					// TODO Auto-generated method stub
					
				}
			});
			        	
		}
		appView = new ApplicationView(this);
	}

	protected void onError(Throwable throwable) {
		ErrorDialog dialog = new ErrorDialog(throwable);
		dialog.showDialog();
	}

	private void onInit(AppEvent event) {
		forwardToView(appView, event);
	}

}
