package com.redspr.redquerybuilder.js.client;

import org.junit.Ignore;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;
import com.redspr.redquerybuilder.core.client.command.CommandBuilder;
import com.redspr.redquerybuilder.core.client.table.TableFilter;

@Ignore
public class AbstractTest extends GWTTestCase {

    @Override
    public void gwtSetUp() {
        TableFilter.resetAlias();
    }


    // XXX not sure about this
    protected JsConfiguration conf;

    protected CommandBuilder builder;



    public static native void addHandlers(JsConfiguration config) /*-{
        config.onSqlChange = function(sql, args) {
            config.lastSql = sql;
            config.lastArgs = args;
        }
    }-*/;

    public static native String getLastSql(JsConfiguration config) /*-{
        return config.lastSql;
    }-*/;

    public static native JsArrayMixed getLastArgs(JsConfiguration config) /*-{
        return config.lastArgs;
    }-*/;

    protected void test(String json, String sql, JsArrayMixed args, String msg) throws Throwable {
        RootPanel.get().getElement().setAttribute("id", "rqb");

        try {
            if (json != null) {
                conf = (JsConfiguration) JsonUtils.unsafeEval(json);
                addHandlers(conf);
            }
            builder = RedQueryBuilder.configure(conf, sql, args);
            assertTrue(builder != null);
            if (msg != null) {
                fail("Was expecting the error message: " + msg);
            }
        } catch (Throwable th) {
            if (msg != null) {
                assertEquals(msg, th.getMessage());
            } else {
                throw th;
            }
        }
    }

    protected JsArrayMixed args(Object... args) {
        JsArrayMixed result = (JsArrayMixed) JavaScriptObject.createArray();

        for (Object x : args) {
            if (x instanceof String) {
                result.push((String) x);
            } else {
                result.push((JavaScriptObject) x);
            }
        }
        return result;
    }

    protected void test(String json, String msg) throws Throwable {
        test(json, "SELECT id FROM Foo", null, msg);
    }

    @Override
    public String getModuleName() {
        return "com.redspr.redquerybuilder.js.RedQueryBuilder";
    }
}
