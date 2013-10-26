package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsColumn extends JavaScriptObject {
    protected JsColumn() {
    }

    public final native String getName() /*-{ return this.name; }-*/;

    public final native String getLabel() /*-{ return this.label; }-*/;

    public final native String getType() /*-{ return this.type; }-*/;

    public final native String getEditor() /*-{ return this.editor; }-*/;

    public final native String getStyleName() /*-{ return this['class']; }-*/;
}
