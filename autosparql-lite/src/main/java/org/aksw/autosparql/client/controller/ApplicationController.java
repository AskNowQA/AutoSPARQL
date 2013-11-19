package org.aksw.autosparql.client.controller;

import java.util.SortedSet;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.aksw.autosparql.client.AppEvents;
import org.aksw.autosparql.client.AutoSPARQLService;
import org.aksw.autosparql.client.AutoSPARQLServiceAsync;
import org.aksw.autosparql.client.view.ApplicationView;
import org.aksw.autosparql.client.widget.ErrorDialog;
import org.aksw.autosparql.client.widget.WaitDialog;
import org.aksw.autosparql.shared.Example;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.google.gwt.logging.client.HtmlLogFormatter;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public class ApplicationController extends Controller
{	
//	private static final boolean useFallbackEndpoint = true;//new Boolean(Application.properties.getProperty("useDBpediaLiveDefault"));
	private ApplicationView appView;
	private Logger log = Logger.getLogger(ApplicationController.class.toString());

	public ApplicationController()
	{
		// configure logging
		final HTML html = new HTML();
		Logger.getLogger("").addHandler(new Handler() {
			{
				// set the formatter, in this case HtmlLogFormatter
				setFormatter(new HtmlLogFormatter(true));
				setLevel(Level.ALL);
			}
			@Override
			public void publish(LogRecord record) {
				if (!isLoggable(record)) {
					Formatter formatter = getFormatter();
					String msg = formatter.format(record);
					html.setHTML(msg);
				}
			}

			@Override
			public void flush()
			{


			}

			@Override
			public void close() 
			{
			}
		});
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
		String query = Window.Location.getParameter("query");
//		String useDBpediaLiveParameter = Window.Location.getParameter("dbpedialive");
		//String endpointParameter = Window.Location.getParameter("endpoint");		
//		boolean useDBpediaLive = useDBpediaLiveParameter==null?useFallbackEndpoint:"on".equals(useDBpediaLiveParameter);
		boolean fastSearch = "on".equals(Window.Location.getParameter("fastsearch"));
		boolean oxford = "on".equals(Window.Location.getParameter("oxford"));
		log.info("oxford: "+oxford);
//		log.info("dbpedia live: "+useDBpediaLive);
		log.info("fastsearch: "+fastSearch);
		final AutoSPARQLServiceAsync service = AutoSPARQLService.Util.getInstance();

		AsyncCallback<Void> callback = new AsyncCallback<Void>()
				{@Override	public void onSuccess(Void result)	{}@Override	public void onFailure(Throwable caught){throw new RuntimeException(caught);}}; 
				service.setFastSearch(fastSearch, callback);
				service.setOxford(oxford, callback);
				
//				service.setUseDBpediaLive(useDBpediaLive, callback);

				//useDBpediaLive,useFastSearch

				if(query==null||query.isEmpty())
				{

				}
				else
				{
					//appView.display(Collections.<Example>emptyList());
					final WaitDialog wait = new WaitDialog("Creating table");
					wait.show();
					log.info("Client: Getting initial examples with query \""+query+"\"...");
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
							wait.hide();
							Window.alert(arg0.getMessage());
							log.severe(arg0.getCause().getMessage());
							throw new RuntimeException(arg0);							
							//log.severe(arg0.getCause().toString());
							
							//appView.showError("could not get examples");
							// TODO Auto-generated method stub
						}
					});

				}
				appView = new ApplicationView(this);
	}

	protected void onError(Throwable throwable)
	{
		ErrorDialog dialog = new ErrorDialog(throwable);
		dialog.showDialog();
	}

	private void onInit(AppEvent event)
	{
		forwardToView(appView, event);
	}

}