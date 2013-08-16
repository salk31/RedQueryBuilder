package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsOperator extends JavaScriptObject {
    protected JsOperator() {
    }

    public final native String getName() /*-{ return this.name; }-*/;

    public final native String getLabel() /*-{ return this.label; }-*/;

    public final native String getCardinality() /*-{ return this.cardinality; }-*/;
}
