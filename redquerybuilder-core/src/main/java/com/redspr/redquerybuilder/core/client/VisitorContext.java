package com.redspr.redquerybuilder.core.client;

import com.google.gwt.core.client.js.JsType;
import com.google.gwt.user.client.ui.HasValue;

/**
 *
 * EXPERIMENTAL - likely to change
 *
 * @param <T>
 */
@JsType
public interface VisitorContext<T> {
    interface NodeType {
        String PARAMETER = "PARAMETER";
        String COLUMN = "COLUMN";
        String COMPARISON = "COMPARISON";
        String LOGIC = "LOGIC"; // XXX not sure about this
        String SELECT = "SELECT";
        String FROM = "FROM";
        String WHERE = "WHERE";
        String ON = "ON";
        String TABLE = "TABLE";
    };

    String getNodeType(); // TODO __ enum and JS?
    // TODO __ what is the definition of this?
    String getNodeName();

    HasMessages asHasMessages();

    HasValue<T> asHasValue();
}
