package org.autosparql.tbsl;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.vaadin.appfoundation.authentication.data.User;
import org.vaadin.appfoundation.authentication.exceptions.PasswordRequirementException;
import org.vaadin.appfoundation.authentication.exceptions.PasswordsDoNotMatchException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortPasswordException;
import org.vaadin.appfoundation.authentication.exceptions.TooShortUsernameException;
import org.vaadin.appfoundation.authentication.exceptions.UsernameExistsException;
import org.vaadin.appfoundation.authentication.util.UserUtil;
import org.vaadin.appfoundation.persistence.facade.FacadeFactory;

public class TBSLContextListener implements ServletContextListener{
	

	@Override
	public void contextDestroyed(ServletContextEvent e) {
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		
	}
	
	private static String getParameter(ServletContext servletContext, String key, String defaultValue) {
        String value = servletContext.getInitParameter(key);
        return value == null ? defaultValue : value;
    }

	private void init(){
		 // Register facade
        try {
			FacadeFactory.registerFacade("default", true);

			// Set the salt for passwords
			Properties prop = new Properties();
			prop.setProperty("password.salt", "pfew4‰‰#fawef@53424fsd");

			// Set the properties for the UserUtil
			prop.setProperty("password.length.min", "4");
			prop.setProperty("username.length.min", "4");
			
			System.setProperties(prop);
			
			UserUtil.registerUser("demo", "demo", "demo");
			
			System.out.println(FacadeFactory.getFacade().list(User.class));
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (TooShortPasswordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TooShortUsernameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PasswordsDoNotMatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UsernameExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PasswordRequirementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
