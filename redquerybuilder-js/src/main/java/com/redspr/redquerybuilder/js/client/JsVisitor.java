package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.js.JsExport;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.Visitor;
import com.redspr.redquerybuilder.core.client.VisitorContext;

// XXX move into core as DefaultVisitor?
//@JsNamespace("$wnd.rqb")
@com.google.gwt.core.client.js.JsType
public class JsVisitor implements Visitor {

    @JsExport("$wnd.rqb.Visitor")
    public JsVisitor() {

    }

    @Override
    public void handle(BaseSqlWidget w) {
    }

    @Override
    public void visit(VisitorContext<?> context) {
    }

    @Override
    public void endVisit(VisitorContext<?> context) {
    }
}
