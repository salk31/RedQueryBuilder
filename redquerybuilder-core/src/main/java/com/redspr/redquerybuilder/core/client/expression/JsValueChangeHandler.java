package com.redspr.redquerybuilder.core.client.expression;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public final class JsValueChangeHandler extends JavaScriptObject {

    protected JsValueChangeHandler() {

    }

    public static native JsValueChangeHandler create(ValueChangeHandler h) /*-{
        return function() {
            h.@com.google.gwt.event.logical.shared.ValueChangeHandler::onValueChange(Lcom/google/gwt/event/logical/shared/ValueChangeEvent;)(null);
        }
    }-*/;

}
