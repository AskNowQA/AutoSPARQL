package org.autosparql.shared;

public class StringUtils
{
	public static final int MAX_LENGTH = 40;
	
	public static String abbreviate(String s,int maxLength)
	{
		return s.length()>maxLength?(s.substring(0,Math.max(0,maxLength-4))+"..."):s;
	}
	
	public static String abbreviate(String s)
	{
		return s.length()>MAX_LENGTH?(s.substring(0,Math.max(0,MAX_LENGTH-4))+"..."):s;
	}
}