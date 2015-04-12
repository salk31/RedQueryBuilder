package com.redspr.redquerybuilder.core.client.util;

import com.google.gwt.dom.client.Document;
import com.google.gwt.text.client.DoubleParser;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueBox;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Editor;

public class NumberBox extends ValueBox<Double> {

    private static final Double ONE = new Double(1.0);

    // HTML5 input=number fields don't like commas etc
    private static final AbstractRenderer<Double> RENDERER = new AbstractRenderer<Double>() {
        @Override
        public String render(Double object) {
            if (object == null) {
                return "";
            }
            return "" + object;
        }
    };

    public NumberBox(Session session, Column column) {
        super(Document.get().createTextInputElement(), RENDERER,
                DoubleParser.instance());

        // HTML5 browser might pick up on this
        getElement().setAttribute("type", "number");

        Editor editor = column.getEditor();
        Double step = (Double) editor.getAttribute("step");
        if (step == null) {
            step = ONE;
        }
        getElement().setAttribute("step", step.toString());
    }
}
