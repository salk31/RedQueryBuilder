package com.redspr.redquerybuilder.core.client.util;

import com.google.gwt.core.client.JavaScriptObject;

// TODO 02 replace with standard JsArrayString
public class JsStringArray extends JavaScriptObject {
    protected JsStringArray() {
    }

    public final native int size() /*-{
        return this.length;
    }-*/;

    public final native String get(int i) /*-{
        return this[i];
    }-*/;

    public final native void add(String p) /*-{
        this.push(p);
    }-*/;

    public static final JsStringArray create() {
        return (JsStringArray) JavaScriptObject.createArray();
    }
}
