package com.redspr.redquerybuilder.core.client.util;

import java.util.Arrays;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.ValueListBox;
import com.redspr.redquerybuilder.core.shared.meta.HasLabel;

public class ListBox2<T> extends ValueListBox<T> {

    public ListBox2() {
        super(new AbstractRenderer<T>() {
            @Override
            public String render(T object) {
                if (object instanceof HasLabel) {
                    return ((HasLabel) object).getLabel();
                }
                return "" + object;
            }
        });
    }

    public void setAcceptableValues(T[] p) {
        this.setAcceptableValues(Arrays.asList(p));
    }
}
