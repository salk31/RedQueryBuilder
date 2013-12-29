package com.redspr.redquerybuilder.core.client.util;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HasValue;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Editor;

public class NumberBox extends Composite implements HasValue<Double> {

    private static final Double ONE = new Double(1.0);

    private final DoubleBox box = new DoubleBox();

    public NumberBox(Session session, Column column) {
        initWidget(box);

        // HTML5 browser might pick up on this
        this.getElement().setAttribute("type", "number");

        Editor editor = column.getEditor();
        Double step = (Double) editor.getAttribute("step");
        if (step == null) {
            step = ONE;
        }
        getElement().setAttribute("step", step.toString());
        // TODO 05 min, max
        // TODO 05 validators that can contribute html5 values?
        // TODO 05 step
        // TODO __ just onkeyup validator?
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Double> handler) {
        return box.addValueChangeHandler(handler);
    }

    @Override
    public Double getValue() {
        return box.getValue();
    }

    @Override
    public void setValue(Double value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Double value, boolean fireEvents) {
        box.setValue(value, fireEvents);
    }
}
