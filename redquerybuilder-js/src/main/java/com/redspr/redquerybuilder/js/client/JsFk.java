package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.redspr.redquerybuilder.core.client.util.JsStringArray;

public class JsFk extends JavaScriptObject {
    protected JsFk() {
    }

    public final native String getName() /*-{ return this.name; }-*/;
    public final native String getLabel() /*-{ return this.label; }-*/;
    public final native String getReverseLabel() /*-{ return this.reverseLabel; }-*/;

    public final native JsStringArray getForeignKeyNames() /*-{
        return this.foreignKeyNames || this.pkColumnNames;
    }-*/;

    public final native String getReferencedTableName() /*-{
        return this.referencedTableName || this.fkTableName;
    }-*/;

    public final native JsStringArray getReferencedKeyNames() /*-{
        return this.referencedKeyNames || this.fkColumnNames;
    }-*/;
}
