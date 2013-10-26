package com.redspr.redquerybuilder.core.shared.meta;



public abstract class Constraint extends SchemaObjectBase {
    protected Table table;

    // for GWT RPC
    public Constraint() {
    }

    Constraint(String name, Table t) {
        setObjectName(name);
        this.table = t;
    }

    public Table getTable() {
        return table;
    }
}
