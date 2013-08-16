package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsSuggestion extends JavaScriptObject {
    protected JsSuggestion() {
    }

    // TODO 00 work for null value? document
    public final native String getValue() /*-{
        if (this.value) {
            return this.value;
        }
        return this;
    }-*/;

    public final native String getLabel() /*-{
        if (this.label) {
            return this.label;
        }
        return this;
    }-*/;
}
