package org.aksw.autosparql.server;

import org.aksw.autosparql.server.store.Store;

public class AutoSPARQLConfig {
	
	private Store store;
	
	public void setStore(Store store){
		this.store = store;
	}
	
	public Store getStore(){
		return store;
	}

}
