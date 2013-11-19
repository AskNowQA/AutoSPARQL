package org.aksw.autosparql.client;

import org.aksw.autosparql.client.widget.ErrorDialog;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class AsyncCallbackEx<T> implements AsyncCallback<T> {

	@Override
	public void onFailure(Throwable caught) {
		ErrorDialog dialog = new ErrorDialog(caught);
		dialog.showDialog();
	}

	@Override
	public abstract void onSuccess(T result);

}
