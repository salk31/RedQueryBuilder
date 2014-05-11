package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayMixed;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.js.client.conf.JsFrom;

/**
 * Root of JS configuration tree.
 */
public class JsConfiguration extends JavaScriptObject {
    protected JsConfiguration() {
    }

    public final native String getTargetId2() /*-{ return this.targetId; }-*/;

    public final String getTargetId() {
        String id = getTargetId2();
        if (id == null) {
            return "rqb";
        }
        return id;
    }

    public final native JsDatabase getMeta() /*-{ return this.meta; }-*/;

    public final native JsArray<JsEditor> getEditors() /*-{
        return this.editors;
    }-*/;

    public final native JavaScriptObject getOnSuggest() /*-{
        return this.suggest
    }-*/;

    public final void fireOnTableChange(ObjectArray<TableFilter> filters) {
        JsArray<JsTableFilter> result = (JsArray<JsTableFilter>) JavaScriptObject.createArray();
        for (int i = 0; i < filters.size(); i++) {
            TableFilter tf = filters.get(i);
            JsTableFilter jtf = JsTableFilter.create(tf.getAlias(), tf.getTable().getName());
            result.push(jtf);
        }
        fireOnTableChange2(result);
    }



    private native JavaScriptObject fireOnTableChange2(JsArray<JsTableFilter> filters) /*-{
        if (this.onTableChange) {
            this.onTableChange(filters);
        }
    }-*/;

    public final native void fireOnSqlChange(String sql, JsArrayMixed args) /*-{
        if (this.onSqlChange) {
            this.onSqlChange(sql, args);
        }
    }-*/;

    // XXX copy n paste
    public final native void fireDefaultSuggest(String tableName, String columnName, String columnTypeName, String query,
            int limit, JsCallback jsCallback) /*-{
        if (this.defaultSuggest) {
        var arg = {tableName: tableName,
                columnName: columnName,
                columnTypeName : columnTypeName,
                query: query,
                limit:limit};
        this.defaultSuggest(arg, function response(s) {
            jsCallback.@com.redspr.redquerybuilder.js.client.JsCallback::response(Lcom/google/gwt/core/client/JavaScriptObject;)(s);
        });
        }
    }-*/;

    public final native void fireSuggest(String tableName, String columnName, String columnTypeName, String query,
            int limit, JsCallback jsCallback) /*-{
        var arg = {tableName: tableName,
                columnName: columnName,
                columnTypeName : columnTypeName,
                query: query,
                limit:limit};
        this.suggest(arg, function response(s) {
            jsCallback.@com.redspr.redquerybuilder.js.client.JsCallback::response(Lcom/google/gwt/core/client/JavaScriptObject;)(s);
        });
    }-*/;

    public final native void fireEnumerate(String tableName, String columnName, String columnTypeName,
            JsCallback jsCallback) /*-{
        if (this.enumerate) {
            var arg = {tableName: tableName,
                    columnName: columnName,
                    columnTypeName : columnTypeName};
            this.enumerate(arg, function response(s) {
                jsCallback.@com.redspr.redquerybuilder.js.client.JsCallback::response(Lcom/google/gwt/core/client/JavaScriptObject;)(s);
            });
        }
    }-*/;

    public final native JsFrom getFrom() /*-{
        return this.from;
    }-*/;
}
