/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.autosparql.tbsl;

import java.util.Map;

import org.autosparql.tbsl.util.URLParameters;
import org.autosparql.tbsl.view.MainView;
import org.vaadin.appfoundation.authentication.SessionHandler;
import org.vaadin.appfoundation.authorization.Permissions;
import org.vaadin.appfoundation.authorization.jpa.JPAPermissionManager;
import org.vaadin.appfoundation.view.ViewHandler;

import com.vaadin.Application;
import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class TBSLApplication extends Application implements ParameterHandler{
    private MainView mainView;
    
    Action action_query = new ShortcutAction("Ctrl+Q",
            ShortcutAction.KeyCode.Q,
            new int[] { ShortcutAction.ModifierKey.CTRL });
    
    
    private String endpoint;
    private String question;

    @Override
    public void init()
    {
    	// Create the application data instance
        UserSession sessionData = new UserSession(this);
        // Register it as a listener in the application context
        getContext().addTransactionListener(sessionData);
        
    	setTheme("custom");
    	
    	ViewHandler.initialize(this);
		SessionHandler.initialize(this);
		Permissions.initialize(this, new JPAPermissionManager());
		
		Window mainWindow = new Window("AutoSPARQL TBSL"); 
		mainWindow.addParameterHandler(this);
		setMainWindow(mainWindow);
		
		mainView = new MainView();
		mainWindow.setContent(mainView);
		mainWindow.setSizeFull();
        
        setLogoutURL("http://aksw.org");
        
        
        
        mainWindow.addActionHandler(new Action.Handler() {
			
			@Override
			public void handleAction(Action action, Object sender, Object target) {
				if(action == action_query){
					onShowLearnedQuery();
				} 
				
			}
			
			@Override
			public Action[] getActions(Object target, Object sender) {
				return new Action[] { action_query};
			}
		});
        
    }
    
    private void onShowLearnedQuery(){
    	String learnedSPARQLQuery = UserSession.getManager().getLearnedSPARQLQuery();
    	VerticalLayout layout = new VerticalLayout();
    	final Window w = new Window("Learned SPARQL Query", layout);
    	w.setWidth("300px");
    	w.setSizeUndefined();
        w.setPositionX(200);
        w.setPositionY(100);
        getMainWindow().addWindow(w);
        w.addListener(new Window.CloseListener() {
			
			@Override
			public void windowClose(CloseEvent e) {
				getMainWindow().removeWindow(w);
			}
		});
        Label queryLabel =  new Label(learnedSPARQLQuery, Label.CONTENT_PREFORMATTED);
        queryLabel.setWidth(null);
        layout.addComponent(queryLabel);
        
        Label nlLabel = new Label(UserSession.getManager().getNLRepresentation(learnedSPARQLQuery));
        layout.addComponent(nlLabel);
        
    }
    
    @Override
    public void terminalError(Terminal.ErrorEvent event) {
        // Call the default implementation.
        super.terminalError(event);

        // Some custom behaviour.
//        if (getMainWindow() != null) {
//            getMainWindow().showNotification(
//                    "An unchecked exception occured!",
//                    event.getThrowable().toString(),
//                    Notification.TYPE_ERROR_MESSAGE);
//        }
    }

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		String[] endpointArray = parameters.get(URLParameters.ENDPOINT);
		if(endpointArray != null && endpointArray.length == 1){
			endpoint = endpointArray[0];
			System.out.println("URL param: " + endpoint);
		}
		String[] questionArray = parameters.get(URLParameters.QUESTION);
		if(questionArray != null && endpointArray.length == 1){
			question = questionArray[0];
			System.out.println("URL param: " + question);
		}
		if(endpoint != null && question != null){
        	mainView.initWithParams(endpoint, question);
        }
		
	}
    
}
