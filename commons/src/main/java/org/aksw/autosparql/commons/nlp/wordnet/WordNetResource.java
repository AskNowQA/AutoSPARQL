package org.aksw.autosparql.commons.nlp.wordnet;

import java.io.IOException;
import java.util.Iterator;

import edu.mit.jwi.data.IContentType;
import edu.mit.jwi.data.ILoadableDataSource;
import edu.mit.jwi.item.IVersion;

public class WordNetResource<T> implements ILoadableDataSource<T>
{

	public WordNetResource(String resource, IContentType<T> type)
	{

	}

	@Override public IContentType<T> getContentType()
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public String getLine(String arg0)
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public String getName()
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public Iterator<String> iterator(String arg0)
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public IVersion getVersion()
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public Iterator<String> iterator()
	{
		if(1==1) throw new RuntimeException("not implemented");return null;
	}

	@Override public boolean isOpen()
	{
		if(1==1) throw new RuntimeException("not implemented");return false;
	}

	@Override public boolean open() throws IOException
	{
		if(1==1) throw new RuntimeException("not implemented");return false;
	}

	@Override public void close()
	{}

	@Override public boolean isLoaded()
	{
		if(1==1) throw new RuntimeException("not implemented");return false;
	}

	@Override public void load()
	{if(1==1) throw new RuntimeException("not implemented");}

	@Override public void load(boolean arg0) throws InterruptedException
	{if(1==1) throw new RuntimeException("not implemented");}

}