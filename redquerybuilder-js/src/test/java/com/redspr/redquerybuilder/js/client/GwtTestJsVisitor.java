package com.redspr.redquerybuilder.js.client;

import org.junit.Test;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtTestJsVisitor extends AbstractTest {

    @Test
    public void testReadArguments() throws Throwable {

        RootPanel.get().getElement().setAttribute("id", "rqb");
        //test(json, "SELECT priority FROM ticket WHERE priority IN (?, ?)",
        //        args("foo", "bar"), null);

        JsConfiguration config = new VisitorJs().config();
        JavaScriptObject result2 = new VisitorJs().start(config, "SELECT priority FROM ticket WHERE priority IN (?, ?)",
                args("foo", "bar"));

        String result = new VisitorJs().visitValues(result2);

        assertEquals("([foo, bar])", result);
    }

    @Test
    public void testAddMessages() {
        RootPanel.get().getElement().setAttribute("id", "rqb");
        //test(json, "SELECT priority FROM ticket WHERE priority IN (?, ?)",
        //        args("foo", "bar"), null);

        JsConfiguration config = new VisitorJs().config();
        JavaScriptObject result2 = new VisitorJs().start(config, "SELECT priority FROM ticket WHERE priority IN (?, ?)",
                args("foo", "bar"));

        String result = new VisitorJs().visitMessages(result2);

        String html = RootPanel.get().getElement().getInnerHTML();
        assertTrue(html.contains("Magical message"));
    }

    @Test
    public void testSerialise() throws Exception {
        // XXX copy n paste
        RootPanel.get().getElement().setAttribute("id", "rqb");

        JsConfiguration config = new VisitorJs().config();
        JavaScriptObject result2 = new VisitorJs().start(config, "SELECT priority FROM ticket WHERE priority IN (?, ?) AND priority = ?",
              args("foo", "bar"));

        String result = new VisitorJs().visitSerialise(result2);
        assertEquals("(SELECT(FROM(TABLE:ticket))(WHERE(LOGIC:AND(COMPARISON:IN(COLUMN:PRIORITY)(PARAMETER:?))(COMPARISON:=(COLUMN:PRIORITY)(PARAMETER:?)))))", result);
    }

    @Test
    public void testSerialiseFrom() throws Exception {
        // XXX copy n paste
        RootPanel.get().getElement().setAttribute("id", "rqb");

        String json = Resources.INSTANCE.synchronous().getText();

        JsConfiguration config = new VisitorJs().config(json);

        JavaScriptObject result2b = new VisitorJs().start(config, "SELECT priority FROM ticket t JOIN user u ON t.owner_id = u.id WHERE u.id = ?",
              args("foo", "bar"));

        String result = new VisitorJs().visitSerialise(result2b);

        assertEquals("(SELECT(FROM(TABLE:ticket T)(TABLE:user U(ON(COMPARISON:=(COLUMN:T.OWNER_ID)(COLUMN:U.ID)))))(WHERE(COMPARISON:=(COLUMN:U.ID)(PARAMETER:?))))", result);
    }
}
