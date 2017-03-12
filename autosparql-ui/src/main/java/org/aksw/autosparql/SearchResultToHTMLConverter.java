package org.aksw.autosparql;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import org.aksw.autosparql.search.SearchResultExtended;

/**
 * @author Lorenz Buehmann
 */
public class SearchResultToHTMLConverter implements Converter<String, SearchResultExtended> {
	@Override
	public Result<SearchResultExtended> convertToModel(String value, ValueContext context) {
		return null;
	}

	@Override
	public String convertToPresentation(SearchResultExtended searchResult, ValueContext context) {
		return
				"<b>" + searchResult.getLabel() + "</b> (" + searchResult.getResource().getURI() + ")</br>" +
						"Types: " + searchResult.getTypes() + "</br>" +
						"<p class=\"wrap\">" + searchResult.getComment() + "</p>";
	}
}
