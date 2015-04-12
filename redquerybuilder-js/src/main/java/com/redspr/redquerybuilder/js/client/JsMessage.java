package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.js.JsExport;
import com.redspr.redquerybuilder.core.client.Message;


public class JsMessage implements Message {
    private String text;

    @JsExport("$wnd.rqb.Message")
    public JsMessage(String text) {
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
