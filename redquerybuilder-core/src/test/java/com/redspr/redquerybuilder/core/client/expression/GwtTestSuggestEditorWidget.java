package com.redspr.redquerybuilder.core.client.expression;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.redspr.redquerybuilder.core.client.AbstractTest;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;

public class GwtTestSuggestEditorWidget extends AbstractTest {

    @Test
    public void testDirtyKeepsValue() throws Exception {
        Session sess = getSession();

        Column col = sess.getDatabase().getMainSchema()
                .findTableOrView("PERSON").getColumn("county");

        final SuggestEditorWidget sew = new SuggestEditorWidget(sess, col);

        final List events = new ArrayList();
        sew.addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                events.add(event.getValue());
            }
        });

        RootPanel.get().add(sew);

        Element elmt = sew.getElement();

        elmt.dispatchEvent(Document.get().createFocusEvent());

        elmt.setAttribute("value", "A");
        elmt.dispatchEvent(Document.get().createKeyUpEvent(false, false, false, false, 'C'));

        List<Element> elmts = find(elmt.getParentElement(), "item");
        assertEquals(3, elmts.size());
        elmts.get(0).dispatchEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false));

        this.delayTestFinish(10000);
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            @Override
            public boolean execute() {
                assertEquals(1, events.size());
                finishTest();
                return false;
            }
        }, 100);
    }
}
