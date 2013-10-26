package com.redspr.redquerybuilder.core.shared.meta;

import com.redspr.redquerybuilder.core.client.engine.Session;

/**
 * The base class for all database objects.
 */
public abstract class DbObjectBase implements DbObject, HasLabel {

    private String objectName;

    private String label;

    protected void setObjectName(String name) {
        objectName = name;
    }

    public void setLabel(String p) {
        this.label = p;
    }

    @Override
    public String getSQL() {
        return Session.quoteIdentifier(objectName);
    }

    @Override
    public String getName() {
        return objectName;
    }

    @Override
    public String toString() {
        return objectName + ":" + super.toString();
    }

    @Override
    public String getLabel() {
        if (label != null) {
            return label;
        }
        return objectName;
    }
}
