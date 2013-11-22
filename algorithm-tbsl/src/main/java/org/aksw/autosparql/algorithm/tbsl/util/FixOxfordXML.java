package org.aksw.autosparql.algorithm.tbsl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import org.apache.commons.lang.StringEscapeUtils;

//
public class FixOxfordXML
{

	public static void main(String[] args) throws FileNotFoundException
	{
		try(Scanner in = new Scanner(new File("src/main/resources/tbsl/evaluation/oxford_working_questions.xml")))
		{
			boolean inQuery=false;
			while(in.hasNextLine())
			{
				String line = in.nextLine();
				if(line.contains("<query>")||line.contains("</query>")) inQuery=!inQuery;
				else if(inQuery)
				{
					line=StringEscapeUtils.escapeXml(line); 
				}
				System.out.println(line);
			}
		}

	}

}