package org.aksw.autosparql.tbsl.gui.vaadin.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class Labels {


	private static Cache<String, String> cache = CacheBuilder.newBuilder().maximumSize(200).concurrencyLevel(1).build();
	private static SimpleIRIShortFormProvider sfp = new SimpleIRIShortFormProvider();

	public static String getLabel(final String uri){
		String label = null;
		try {
			label = cache.get(uri, new Callable<String>() {
				@Override
				public String call() throws Exception {
					String shortForm = sfp.getShortForm(IRI.create(uri));
					String regex = "([a-z])([A-Z])";
			        String replacement = "$1 $2";
			        String label = shortForm.replaceAll(regex, replacement).toLowerCase();
			        return label;
				}
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return label;

	}

	public static String getLabelForResource(final String uri){
		String label = null;
		try {
			label = cache.get(uri, new Callable<String>() {
				@Override
				public String call() throws Exception {
					String decodedURI = uri;
	                try {
						decodedURI = URLDecoder.decode(decodedURI, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String shortForm = sfp.getShortForm(IRI.create(decodedURI));
					String regex = "([a-z])([A-Z])";
			        String replacement = "$1 $2";
//			        String label = shortForm.replaceAll(regex, replacement).toLowerCase();
			        String label = shortForm.replace("_", " ");
			        label = upperCaseWordFirst(label);
			        return label;
				}
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return label;

	}

	public static String upperCaseWordFirst(String str) {
	    StringBuffer sb = new StringBuffer();
	    Matcher m = Pattern.compile
	        ("([a-z])([a-z]*)",Pattern.CASE_INSENSITIVE).matcher(str);
	    while (m.find()) {
	        m.appendReplacement(sb, m.group(1).toUpperCase()
	            + m.group(2).toLowerCase()) ;
	    }
	    str = m.appendTail(sb).toString();
	    return str;
	  }

}
