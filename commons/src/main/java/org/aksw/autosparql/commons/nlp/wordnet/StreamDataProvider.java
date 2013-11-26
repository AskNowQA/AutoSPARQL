package org.aksw.autosparql.commons.nlp.wordnet;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import edu.mit.jwi.data.IContentType;
import edu.mit.jwi.data.IDataProvider;
import edu.mit.jwi.data.IDataSource;
import edu.mit.jwi.item.IVersion;

public class StreamDataProvider implements IDataProvider
{

	@Override public IVersion getVersion()
	{
		return null;
	}

	@Override public boolean isOpen()
	{
		return false;
	}

	@Override public boolean open() throws IOException
	{
		return false;
	}

	@Override public void close()
	{}

	@Override public URL getSource()
	{
		return null;
	}

	@Override public <T> IDataSource<T> getSource(IContentType<T> arg0)
	{
		return null;
	}

	@Override public Set<? extends IContentType<?>> getTypes()
	{
		return null;
	}

	@Override public void setSource(URL arg0)
	{}

}
