package com.redspr.redquerybuilder.core.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;

public class GwtTestDom extends AbstractTest {


    @Test
    public void testTextBox() throws Exception {
        TextBox tb = new TextBox();
        RootPanel.get().add(tb);
        final List events = new ArrayList();
        tb.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                events.add(event);
            }
        });

        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), tb);

        assertEquals(1, events.size());
    }




    @Test
    public void testSuggestBox() throws Exception {
        this.delayTestFinish(1000);


        MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
        oracle.add("Cat");
        oracle.add("Dog");
        oracle.add("Horse");
        oracle.add("Canary");

        SuggestBox box = new SuggestBox(oracle);


        RootPanel.get().add(box);
        final List events = new ArrayList();
        box.addValueChangeHandler(new ValueChangeHandler() {
            @Override
            public void onValueChange(ValueChangeEvent event) {
                events.add(event);
            }
        });

        box.addSelectionHandler(new SelectionHandler() {

            @Override
            public void onSelection(SelectionEvent event) {
               events.add(event);
               finishTest();
            }

        });

        box.getElement().dispatchEvent(Document.get().createChangeEvent());
        assertEquals(1, events.size());

        box.getElement().setAttribute("value", "Ca");
        box.getElement().dispatchEvent(Document.get().createKeyUpEvent(false, false, false, false, 'C'));

        //System.out.println("X=" + box.getElement().getParentElement().getInnerHTML());
        List<Element> elmts = find(box.getElement(), "item");
        assertEquals(2, elmts.size());
        elmts.get(0).dispatchEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false));
// event fired as finally..
        //assertEquals(2, events.size());
    }

}
