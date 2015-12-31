/*
 * Diamond User Administration System
 * Copyright © 2015 Diamond Light Source Ltd
 */

package com.redspr.redquerybuilder.core.client;

/**
 *
 *
 * @author yjs77802
 */
public class VisitorContextBase implements VisitorContext {
    private final String nodeType;

    private final String nodeValue;

    public VisitorContextBase(String type, String value) {
        this.nodeType = type;
        this.nodeValue = value;
    }

    @Override
    public String getNodeType() {
        return nodeType;
    }

    @Override
    public String getNodeName() {
        return nodeValue;
    }
}
