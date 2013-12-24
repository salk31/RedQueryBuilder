package com.redspr.redquerybuilder.js.client;

import java.util.AbstractList;
import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsDate;

public class JsList extends AbstractList {

    public static final class JsListAdapter extends JavaScriptObject {
        protected JsListAdapter() {
        }

        public native int length() /*-{ return this.length; }-*/;

        public native boolean isNull(int i) /*-{
            return this[i] == null;
        }-*/;

        public native boolean isDate(int i) /*-{
            return Boolean(this[i].getMonth);
        }-*/;

        public Date getDate(int i) {
            JsDate jsDate = getJsDate(i);

            return new Date((long) jsDate.getTime());
        }

        private native JsDate getJsDate(int i) /*-{
            return this[i];
        }-*/;

        public native boolean isString(int i) /*-{
            return (typeof this[i] == 'string' || this[i] instanceof String);
        }-*/;

        public native String getString(int i) /*-{
            return this[i];
        }-*/;

        public native boolean isNumber(int i) /*-{
            return (typeof this[i] == 'number' || this[i] instanceof Number);
        }-*/;

        public native double getNumber(int i) /*-{
            return this[i];
        }-*/;

        public native String debugString(int i) /*-{
            return 'typeof=' + typeof this[i] + ' toString="' + this[i] + '"';
        }-*/;
    }

    private final JsListAdapter target;

    public JsList(JavaScriptObject target) {
        this.target = (JsListAdapter) target;
    }

    @Override
    public Object get(int i) {
        if (target.isNull(i)) {
            return i;
        } else if (target.isDate(i)) {
            return target.getDate(i);
        } else if (target.isString(i)) {
            return target.getString(i);
        } else if (target.isNumber(i)) {
            return target.getNumber(i);
        }

        throw new RuntimeException("Unable to handle value " + target.debugString(i));
    }

    @Override
    public int size() {
        return target.length();
    }
}
