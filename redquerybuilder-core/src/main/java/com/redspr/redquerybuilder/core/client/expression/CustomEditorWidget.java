package com.redspr.redquerybuilder.core.client.expression;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;

public class CustomEditorWidget extends FlowPanel implements HasValue<String> {

//    public static final class Factory extends JavaScriptObject {
//        public native Instance create() /*-{
//            return this();
//        }-*/;
//    }

    public static final class Instance extends JavaScriptObject {
        protected Instance() {

        }
        public static native Instance invoke(JavaScriptObject create, Element elmt) /*-{
            return create(elmt);
        }-*/;

        public native String getValue() /*-{
            return this.getValue();
        }-*/;

        public native void setValue(String value) /*-{
            if (this.setValue) {
                this.setValue(value);
            }
        }-*/;

        public native void addValueChangeHandler(JavaScriptObject handler) /*-{
            if (this.addValueChangeHandler) {
                this.addValueChangeHandler(handler);
            }
        }-*/;
    }

    private final Instance instance;

    public CustomEditorWidget(Session session, Column column) {
        JavaScriptObject create = (JavaScriptObject) column.getEditor().getAttribute("create");
        if (create == null) {
            throw new IllegalArgumentException("Custom editor needs a 'create' function");
        }

        instance = Instance.invoke(create, this.getElement());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<String> handler) {
        instance.addValueChangeHandler(JsValueChangeHandler.create(handler));
        return null;
    }

    @Override
    public String getValue() {
        return instance.getValue();
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        instance.setValue(value);
    }


}
