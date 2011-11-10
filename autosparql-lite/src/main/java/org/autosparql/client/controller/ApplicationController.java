package org.autosparql.client.controller;

import java.util.SortedSet;
import java.util.logging.Logger;

import org.autosparql.client.AppEvents;
import org.autosparql.client.AutoSPARQLService;
import org.autosparql.client.AutoSPARQLServiceAsync;
import org.autosparql.client.view.ApplicationView;
import org.autosparql.client.widget.ErrorDialog;
import org.autosparql.client.widget.WaitDialog;
import org.autosparql.shared.Example;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ApplicationController extends Controller
{
	private ApplicationView appView;
	private Logger log = Logger.getLogger(ApplicationController.class.toString());

	public ApplicationController()
	{
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
	
//	public void learn()
//	{
//		
//	}
	
	public void initialize()
	{
		String query = com.google.gwt.user.client.Window.Location.getParameter("query");
		boolean useDBpediaLive = "on".equals(com.google.gwt.user.client.Window.Location.getParameter("dbpedialive"));
		boolean fastSearch = "on".equals(com.google.gwt.user.client.Window.Location.getParameter("fastsearch"));
		log.info("dbpedia live: "+useDBpediaLive);
		log.info("fastsearch: "+fastSearch);
		final AutoSPARQLServiceAsync service = AutoSPARQLService.Util.getInstance();
		
		AsyncCallback<Void> callback = new AsyncCallback<Void>()
			{@Override	public void onSuccess(Void result)	{}@Override	public void onFailure(Throwable caught){}}; 
		service.setFastSearch(fastSearch, callback);
		service.setUseDBpediaLive(useDBpediaLive, callback);
		
			//useDBpediaLive,useFastSearch
		
		if(query==null||query.isEmpty())
		{
			
		}
		else
		{
			//appView.display(Collections.<Example>emptyList());
			final WaitDialog wait = new WaitDialog("Creating table");
			wait.show();
			log.info("Getting initial examples...");
			service.getExamples(query, new AsyncCallback<SortedSet<Example>>() {				
				@Override
				public void onSuccess(SortedSet<Example> examples)
				{
					//new Example("testuri", "testlabel", "testimageurl", "testcomment");
					wait.hide();
					log.info("successfully gotten "+examples.size()+" initial examples, displaying them now.");
					appView.display(examples);
					log.info("successfully displayed the initial examples");
				}
			
				@Override
				public void onFailure(Throwable arg0)
				{
					log.severe(arg0.getCause().getMessage());
					//log.severe(arg0.getCause().toString());
					wait.hide();
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
