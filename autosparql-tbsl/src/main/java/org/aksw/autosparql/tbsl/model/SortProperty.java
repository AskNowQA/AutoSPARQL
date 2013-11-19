package org.aksw.autosparql.tbsl.model;

public enum SortProperty {
	
	PRICE_DESC("price", "price(highest first)", false),
	PRICE_ASC("price", "price(lowest first)", true),
	BEDROOMS_DESC("bedrooms", "#bedrooms(highest first)", false),
	BEDROOMS_ASC("bedrooms", "#bedrooms(lowest first)", true),
	BATHROOMS_DESC("bathrooms", "#bathrooms(highest first)", false),
	BATHROOMS_ASC("bathrooms", "#bathrooms(lowest first)", true),
	RECEPTIONS_DESC("receptions", "#receptions(highest first)", false),
	RECEPTIONS_ASC("receptions", "#receptions(lowest first)", true);
	
	private String id;
	private String label;
	private boolean ascending;
	
	
	SortProperty(String id, String label, boolean ascending) {
		this.id = id;
		this.label = label;
		this.ascending = ascending;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean isAscending() {
		return ascending;
	}

}
