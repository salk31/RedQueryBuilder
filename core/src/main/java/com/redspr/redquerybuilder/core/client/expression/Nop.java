package com.redspr.redquerybuilder.core.client.expression;


import java.util.List;

import com.google.gwt.user.client.ui.Label;

public class Nop extends Expression {
    public Nop() {
        super(null);
        initWidget(new Label());
    }

    @Override
    public String getSQL(List args) {
        return "1=1";
    }
}
