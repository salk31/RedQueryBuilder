package com.redspr.redquerybuilder.core.client.expression;


import java.util.List;

import com.google.gwt.user.client.ui.Label;
import com.redspr.redquerybuilder.core.client.VisitorContext;

public class Null extends Expression {
    public Null() {
        super(null);
        initWidget(new Label());
    }

    @Override
    public String getSQL(List args) {
        return "NULL";
    }

    @Override
    public String getNodeType() {
        return VisitorContext.NodeType.NULL;
    }

    @Override
    public String getNodeName() {
        return "NULL";
    }
}
