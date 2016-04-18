package com.github.madbrain.gwtpromise.client.rpc;

import com.github.madbrain.gwtpromise.client.BasePromise;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncCallPromise<T>  extends BasePromise<T> implements AsyncCallback<T> {

    @Override
    public void onFailure(Throwable caught) {
        reject(caught);
    }

    @Override
    public void onSuccess(T result) {
        resolve(result);
    }
}
