package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsDatabase extends JavaScriptObject {
    protected JsDatabase() {
    }

    public final native JsArray<JsTable> getTables() /*-{ return this.tables; }-*/;

    public final native JsArray<JsType> getTypes() /*-{ return this.types; }-*/;
}
