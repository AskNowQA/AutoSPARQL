package org.aksw.autosparql.server.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import static org.aksw.autosparql.server.util.TieBreaker.TIEBREAKER;

/** Resolves conflicts (chooses one) between objects of different language tags. Also implements the comparator interface (rating the chosen one smaller).
 No tag is preferred to an unknown tag, but deprived to a known one.
 Lower indices are preferred to higher ones and equal or both unknown tags as well as both no tags are passed through to a tie breaker
 which can also return 0. The default tie breaker prefers the shorter strings.
 Example:
 input: languages: {"de","en"}, s: "Prag@de" t: "Praha"
 output: "Prag@de"
 *  @author Konrad Höffner
 * */
public class LanguageResolver implements Comparator<String>
{
	Logger log = Logger.getLogger(LanguageResolver.class.toString());
	public static 	final List<String> DEFAULT_LANGUAGES = Arrays.asList(new String[] {"de","en"});
	public 			final List<String> languages;
	public final Comparator<String> tiebreaker;
	
	public LanguageResolver() {this(DEFAULT_LANGUAGES);}

	public LanguageResolver(List<String> languages)
	{
		this.languages = languages;
		tiebreaker = TIEBREAKER;
	}

	public LanguageResolver(String[] languages) {this(Arrays.asList(languages));}

	public String resolve(String s,String t)
	{
		String result = compare(s,t)==-1?s:t;
	//	log.info("LanguageResolver comparing \""+abbreviate(s)+"\" and \""+abbreviate(t)+"\". Choosing "+abbreviate(result));
		return result;
	}

	/** returns -1 if <tt>s</tt> has the better language tag then <tt>t</tt>, 1 if it is the other way around.
	 * If the language tags are equal or both missing then a tiebreaker on the lexical forms is used which can also return 0. */
	@Override
	public int compare(String s, String t)
	{
		String[] strings = {s,t};
		String[] lexical = new String[2];
		String[] tag = new String[2];
		
		for(int i=0;i<2;i++)
		{
			int index = strings[i].indexOf('@');	
			if(index==-1||index>=strings[i].length()-1)
			{
				lexical[i]=strings[i];
				tag[i]=null;
			}
			else
			{
				lexical[i]=strings[i].substring(0,index);
				tag[i]=strings[i].substring(index+1);
			}
		}
		if(tag[0]==null&&tag[1]==null) return tiebreaker.compare(lexical[0],lexical[1]);
		if(tag[0]==null) {return languages.contains(tag[1])?1:-1;} // no tag better than unknown tag
		if(tag[1]==null) {return languages.contains(tag[0])?-1:1;} // no tag better than unknown tag
		if(tag[0].equals(tag[1])) return tiebreaker.compare(lexical[0],lexical[1]);
		if(languages.contains(tag[0]))
		{
			if(languages.contains(tag[1]))
			{
				return new Integer(languages.indexOf(tag[0])).compareTo(new Integer(languages.indexOf(tag[1])));
			}
			else {return -1;}			
		}
		if(languages.contains(tag[1])) {return 1;}
		return tiebreaker.compare(lexical[0],lexical[1]);
	}

}
