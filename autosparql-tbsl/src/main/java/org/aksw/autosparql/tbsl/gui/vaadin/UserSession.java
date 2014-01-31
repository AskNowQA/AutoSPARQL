package org.aksw.autosparql.tbsl.gui.vaadin;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;

public class UserSession implements TransactionListener, Serializable {

	private ResourceBundle bundle;
	private Locale locale; // Current locale
	private Application app; // For distinguishing between apps

	private static ThreadLocal<UserSession> instance = new ThreadLocal<UserSession>();
	
	private TBSLManager manager;
	
	public UserSession(TBSLApplication app) {
		this.app = app;

		// It's usable from now on in the current request
		instance.set(this);
		
		manager = new TBSLManager();
	}

	@Override
	public void transactionStart(Application application, Object transactionData) {
		// Set this data instance of this application
		// as the one active in the current thread.
		if (this.app == application)
			instance.set(this);
	}

	@Override
	public void transactionEnd(Application application, Object transactionData) {
		// Clear the reference to avoid potential problems
		if (this.app == application)
			instance.set(null);
	}

	public static void initLocale(Locale locale, String bundleName) {
		instance.get().locale = locale;
		instance.get().bundle = ResourceBundle.getBundle(bundleName, locale);
	}

	public static Locale getLocale() {
		return instance.get().locale;
	}
	
	public static UserSession getCurrentSession() {
		return instance.get();
	}

	public static String getMessage(String msgId) {
		return instance.get().bundle.getString(msgId);
	}
	
	public static TBSLManager getManager(){
		return instance.get().manager;
	}
	
}
