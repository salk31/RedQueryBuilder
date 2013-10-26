package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsEditor extends JavaScriptObject {
    protected JsEditor() {
    }

    public final native String getName() /*-{ return this.name; }-*/;

    public final native String getFormat() /*-{ return this.format; }-*/;

    public final native String getStyleName() /*-{ return this['class']; }-*/;
}
