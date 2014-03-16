package com.redspr.redquerybuilder.js.client;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;
import com.redspr.redquerybuilder.core.client.command.CommandBuilder;
import com.redspr.redquerybuilder.core.client.table.TableFilter;

public class GwtTestBasics extends GWTTestCase {

// TODO __ test putting dates, numbers into the args

    // XXX not sure about this
    private JsConfiguration conf;

    private CommandBuilder builder;

    @Override
    public void gwtSetUp() {
        TableFilter.resetAlias();
    }

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

    private void test(String json, String sql, JsArrayMixed args, String msg) throws Throwable {
        RootPanel.get().getElement().setAttribute("id", "rqb");

        try {
            conf = (JsConfiguration) JsonUtils.unsafeEval(json);
            addHandlers(conf);
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

    private JsArrayMixed args(Object... args) {
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

    private void test(String json, String msg) throws Throwable {
        test(json, "SELECT id FROM Foo", null, msg);
    }

    @Test
    public void testNothing() throws Exception {
        try {
            RedQueryBuilder.configure(null, null, null);
            fail();
        } catch (Throwable th) {
            assertEquals("Config is null.", th.getMessage());
        }
    }

    @Test
    public void testEmptyConfig() throws Throwable {
        test("{}", "Meta is null.");
    }

    @Test
    public void testNoTables() throws Throwable {
        test("{meta:{}}", "Unable to find table FOO");
    }

    @Test
    public void testNoFksInJson() throws Throwable {
        test("{meta:{tables:[{name:'Foo'}]}}", null);
    }

    @Test
    public void testLegacyFk() {
        JsFk fk = (JsFk) JsonUtils.unsafeEval("{pkColumnNames:['sillyName'], fkTableName:'almostAsSilly', fkColumnNames:['middleSilly']}");
        assertEquals("sillyName", fk.getForeignKeyNames().get(0));
        assertEquals("almostAsSilly", fk.getReferencedTableName());
        assertEquals("middleSilly", fk.getReferencedKeyNames().get(0));
    }



    @Test
    public void testInAndArgs() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, "SELECT priority FROM ticket WHERE priority IN (?)", args("foo"), null);

        builder.fireDirty();

        assertEquals(1, getLastArgs(conf).length());
        assertEquals("foo", getLastArgs(conf).getString(0));
    }

    @Test
    public void testArrayParameterIn() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, "SELECT priority FROM ticket WHERE priority IN (?, ?)", args("foo", "bar"), null);

        builder.fireDirty();

        assertEquals(2, getLastArgs(conf).length());
        assertEquals("foo", getLastArgs(conf).getString(0));
        assertEquals("bar", getLastArgs(conf).getString(1));
    }

    @Test
    public void testDateInAndOut() throws Throwable {
        String json = Resources.INSTANCE.minimalDateMeta().getText();
        JsDate dateIn = JsDate.create();
        dateIn.setUTCFullYear(1914);
        dateIn.setUTCMonth(4);
        dateIn.setUTCDate(15);
        dateIn.setUTCHours(22);
        dateIn.setUTCMinutes(13);
        test(json, "SELECT dob FROM person WHERE dob = ?", args(dateIn), null);

        builder.fireDirty();

        assertEquals(1, getLastArgs(conf).length());
        JsDate dateOut = getLastArgs(conf).getObject(0);
        assertEquals(1914, dateOut.getUTCFullYear());
        assertEquals(4, dateOut.getUTCMonth());
        assertEquals(15, dateOut.getUTCDate());
        assertEquals(22, dateOut.getUTCHours());
        assertEquals(13, dateOut.getUTCMinutes());
    }

    public void testNullSqlAndNullArgs() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, null, null, null);

        builder.fireDirty();
    }

    public void testJsList() throws Throwable {
        JsDate dateIn = JsDate.create();
        dateIn.setUTCFullYear(1914);
        dateIn.setUTCMonth(4);
        dateIn.setUTCDate(15);
        dateIn.setUTCHours(22);
        dateIn.setUTCMinutes(13);

        JsArrayMixed mixed = (JsArrayMixed) JsArrayMixed.createArray();
        mixed.push(dateIn);
        mixed.push(new Double(123.12d));
        mixed.push("123");
        mixed.push("false");

        List<Object> list = JsList.get().toList(mixed);
        Date dateOut = (Date) list.get(0);
        assertEquals(14, dateOut.getYear());
        assertEquals(4, dateOut.getMonth());
        assertEquals(15, dateOut.getDate());
        assertEquals(22, dateOut.getHours());
        assertEquals(13, dateOut.getMinutes());

        assertEquals(new Double(123.12d), list.get(1));
        assertEquals("123", list.get(2));
        assertEquals("false", list.get(3));
    }



    @Override
    public String getModuleName() {
        return "com.redspr.redquerybuilder.js.RedQueryBuilder";
    }
}
