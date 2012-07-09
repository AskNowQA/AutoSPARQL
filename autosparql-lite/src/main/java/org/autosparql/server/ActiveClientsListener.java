package org.autosparql.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class ActiveClientsListener implements HttpSessionListener
{	
	private static long hits = 0;
	private static long numberOfConnectedClients = 0;

	public static long hits() {return hits;}
	public static long numberOfConnectedClients() {return numberOfConnectedClients;}
	
	@Override public void sessionCreated(HttpSessionEvent arg0)
	{
		synchronized(ActiveClientsListener.class)
		{
			hits++;
			numberOfConnectedClients++;
		}
	}

	@Override public void sessionDestroyed(HttpSessionEvent arg0)
	{
		synchronized(ActiveClientsListener.class)
		{numberOfConnectedClients--;}
	}
}