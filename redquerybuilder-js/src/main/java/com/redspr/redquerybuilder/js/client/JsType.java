package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JsType extends JavaScriptObject {
    protected JsType() {
    }

    public final native String getName() /*-{ return this.name; }-*/;

    public final native String getEditor() /*-{ return this.editor; }-*/;

    public final native JsArray<JsOperator> getOperators() /*-{ return this.operators; }-*/;

    public final native String getStyleName() /*-{ return this['class']; }-*/;
}
