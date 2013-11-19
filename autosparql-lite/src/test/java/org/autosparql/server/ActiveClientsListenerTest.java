package org.autosparql.server;

import static org.junit.Assert.*;
import org.aksw.autosparql.server.ActiveClientsListener;
import org.junit.Test;

public class ActiveClientsListenerTest
{
	class TestThread extends Thread
	{
		@Override
		public void run()
		{
			ActiveClientsListener listener = new ActiveClientsListener();
			for(int i=0;i<1000;i++)
			{
				listener.sessionCreated(null);
				listener.sessionDestroyed(null);
			}
		}
	}

	@Test
	public void test() throws InterruptedException
	{
		Thread[] threads = new Thread[1000];
		for(int i=0;i<threads.length;i++) {threads[i] = new TestThread(); threads[i].start();}
		boolean working = false;
		do
		{
			working = false;
			for(int i=0;i<threads.length;i++) if(threads[i].isAlive()) {working=true;}			
		}
		while(working);
		assertTrue(ActiveClientsListener.hits()==1000000);
		assertTrue(ActiveClientsListener.numberOfConnectedClients()==0);
	}
}
