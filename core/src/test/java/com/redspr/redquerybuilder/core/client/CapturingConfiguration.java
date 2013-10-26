package com.redspr.redquerybuilder.core.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.redspr.redquerybuilder.core.client.Configuration;
import com.redspr.redquerybuilder.core.shared.meta.EnumerateRequest;

public class CapturingConfiguration extends Configuration {

    private AsyncCallback<Response> enumerateCallback;

    public AsyncCallback<Response> getEnumerateCallback() {
        return enumerateCallback;
    }

    @Override
    public void fireEnumerate(EnumerateRequest request, AsyncCallback<Response> callback) {
        enumerateCallback = callback;
    }
}
