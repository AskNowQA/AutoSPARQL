package org.autosparql.server.util;

import java.util.Comparator;

public class TieBreaker implements Comparator<String>
	{
		public static final TieBreaker TIEBREAKER = new TieBreaker();
		
		@Override
		public int compare(String o1, String o2)
		{
			// TODO Auto-generated method stub
			return 0;
		}
	}