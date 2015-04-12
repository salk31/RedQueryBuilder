package com.redspr.redquerybuilder.core.client;

import com.google.gwt.core.client.js.impl.PrototypeOfJsType;

/**
 *
 * EXPERIMENTAL - likely to change
 */
@com.google.gwt.core.client.js.JsType(prototype = "$wnd.RqbVisitor")
public interface Visitor {

    @PrototypeOfJsType
    static class Prototype implements Visitor {

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

    @Deprecated
    void handle(BaseSqlWidget w);

    void visit(VisitorContext<?> context);

    void endVisit(VisitorContext<?> context);
}