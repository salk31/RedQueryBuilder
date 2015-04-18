package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsTable extends JavaScriptObject {
    protected JsTable() {
    }

    public final native String getName() /*-{ return this.name; }-*/;

    public final native String getLabel() /*-{ return this.label; }-*/;

    public final native JsArray<JsColumn> getColumns() /*-{ return this.columns || []; }-*/;

    public final native JsArray<JsFk> getFks() /*-{ return this.fks || []; }-*/;
}
