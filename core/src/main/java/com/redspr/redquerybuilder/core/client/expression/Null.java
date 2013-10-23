package com.redspr.redquerybuilder.core.client.expression;


import java.util.List;

import com.google.gwt.user.client.ui.Label;

public class Null extends Expression {
    public Null() {
        super(null);
        initWidget(new Label());
    }

    @Override
    public String getSQL(List args) {
        return "NULL";
    }
}
