package org.aksw.autosparql.algorithm.tbsl.sem.drs;

import java.io.StringReader;
import org.aksw.autosparql.algorithm.tbsl.sem.drs.DRS;
import org.aksw.autosparql.algorithm.tbsl.sem.drs.reader.DRSParser;
import org.aksw.autosparql.algorithm.tbsl.sem.drs.reader.ParseException;

public class DRS_Constructor {
	
	public DRS construct(String string)
	{
		DRS drs = null;
		DRSParser parser =  new DRSParser(new StringReader(new String(string)));
		parser.ReInit(new StringReader(new String(string)));		
		try {
			drs = parser.DRS();
		} catch (ParseException e) {
			System.err.println("DRS Parse Exception: " + string);
			e.printStackTrace();
		}
	
		return drs;
	}
	
}
