package com.redspr.redquerybuilder.js.client;

import org.junit.Test;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;
import com.redspr.redquerybuilder.core.client.table.TableFilter;

public class GwtTestBasics extends GWTTestCase {

    @Override
    public void gwtSetUp() {
        TableFilter.resetAlias();
    }

    private void test(String json, String msg) throws Exception {
        RootPanel.get().getElement().setAttribute("id", "rqb");
        try {
            JsConfiguration conf = (JsConfiguration) JsonUtils.unsafeEval(json);
            RedQueryBuilder.configure(conf, "SELECT id FROM Foo", null);
            fail();
        } catch (Throwable th) {
            assertEquals(msg, th.getMessage());
        }
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
    public void testEmptyConfig() throws Exception {
        test("{}", "Meta is null.");
    }

    @Test
    public void testNoTables() throws Exception {
        test("{meta:{}}", "Unable to find table FOO");
    }

    @Test
    public void testNoFksInJson() throws Exception {
        test("{meta:{tables:[{name:'Foo'}]}}", "No tables defined");
    }


    @Override
    public String getModuleName() {
        return "com.redspr.redquerybuilder.js.RedQueryBuilder";
    }
}
