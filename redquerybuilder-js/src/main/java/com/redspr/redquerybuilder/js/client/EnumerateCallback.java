package com.redspr.redquerybuilder.js.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class EnumerateCallback implements JsCallback {
    private final AsyncCallback<Response> callback;

    EnumerateCallback(AsyncCallback<Response> callback2) {
        this.callback = callback2;
    }

    @Override
    public void response(JavaScriptObject raw2) {
        response(raw2, false);
    }

    public void response(JavaScriptObject raw2, boolean hasMore) {
        JsArray<JsSuggestion> raw = (JsArray<JsSuggestion>) raw2;
        List<Suggestion> suggestions = new ArrayList<Suggestion>();

        for (int i = 0; i < raw.length(); i++) {
            JsSuggestion jss = raw.get(i);
            Suggestion s = new MultiWordSuggestion(jss.getValue(), jss.getLabel());
            suggestions.add(s);
        }

        Response response = new Response(suggestions);
        response.setMoreSuggestions(hasMore);
        callback.onSuccess(response);
    }

}