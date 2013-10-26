package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;

public class JsTableFilter extends JavaScriptObject {
    protected JsTableFilter() {
    }

    public final native String getAlias() /*-{ return this.alias; }-*/;

    public final native String getTableName() /*-{ return this.tableName; }-*/;

    public static final native JsTableFilter create(String alias, String tableName) /*-{
        return {alias: alias, tableName: tableName};
    }-*/;
}
