package com.redspr.redquerybuilder.core.client.util;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class XWidget<T extends Widget> extends SimplePanel {
    public T getValue() {
        return (T) getWidget();
    }

    public void setValue(T p) {
        setWidget(p);
    }
}
