package com.redspr.redquerybuilder.js.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsDate;

public final class JsList extends JavaScriptObject {

    private static final JsList INSTANCE = (JsList) JavaScriptObject.createObject();

    public static JsList get() {
        return INSTANCE;
    }

    protected JsList() {
    }

    public native boolean isNull(JavaScriptObject array, int i) /*-{
        return array[i] == null;
    }-*/;

    public native boolean isDate(JavaScriptObject array, int i) /*-{
        return Boolean(array[i].getMonth);
    }-*/;

    public Date getDate(JavaScriptObject array, int i) {
        JsDate jsDate = getJsDate(array, i);

        return new Date((long) jsDate.getTime());
    }

    private native JsDate getJsDate(JavaScriptObject array, int i) /*-{
        return array[i];
    }-*/;

    public native boolean isString(JavaScriptObject array, int i) /*-{
        return (typeof array[i] == 'string' || array[i] instanceof String);
    }-*/;

    public native boolean isNumber(JavaScriptObject array, int i) /*-{
        return (typeof array[i] == 'number' || array[i] instanceof Number);
    }-*/;

    public native String debugString(JavaScriptObject array, int i) /*-{
        return 'typeof=' + typeof array[i] + ' toString="' + array[i] + '"';
    }-*/;

    public JsArrayMixed toJso(List<Object> args) {
        JsArrayMixed result = (JsArrayMixed) JavaScriptObject.createArray();
        for (Object o : args) {
            if (o == null) {
                result.push((JavaScriptObject) null);
            } else if (o instanceof String) {
                result.push((String) o);
            } else if (o instanceof Date) {
                result.push(JsDate.create(((Date) o).getTime()));
            } else if (o instanceof Double) {
                result.push(((Double) o).doubleValue());
            } else {
                throw new IllegalArgumentException("Don't know how to handle "
                        + o);
            }
        }
        return result;
    }

    private Object get(JsArrayMixed args, int i) {
        if (isNull(args, i)) {
            return i;
        } else if (isDate(args, i)) {
            return getDate(args, i);
        } else if (isString(args, i)) {
            return args.getString(i);
        } else if (isNumber(args, i)) {
            return args.getNumber(i);
        }

        throw new RuntimeException("Unable to handle value " + debugString(args, i));
    }

    public List<Object> toList(JsArrayMixed args) {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < args.length(); i++) {
            result.add(get(args, i));
        }
        return result;
    }
}

