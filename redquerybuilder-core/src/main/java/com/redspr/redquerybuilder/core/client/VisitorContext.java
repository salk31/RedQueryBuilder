package com.redspr.redquerybuilder.core.client;

import com.google.gwt.core.client.js.JsType;

/**
 *
 * EXPERIMENTAL - likely to change
 */
@JsType
public interface VisitorContext {
    interface NodeType {
        String NULL = "NULL";
        String PARAMETER = "PARAMETER";
        String COLUMN = "COLUMN";
        String COMPARISON = "COMPARISON";
        String LOGIC = "LOGIC"; // XXX not sure about this
        String SELECT = "SELECT";
        String FROM = "FROM";
        String WHERE = "WHERE";
        String ON = "ON";
        String TABLE = "TABLE";
        String WILDCARD = "WILDCARD";
    };

    String getNodeType();

    /**
     * @return Simple human readable string representing the state of the node. Often the SQL representation.
     */
    String getNodeName();
}
