package com.redspr.redquerybuilder.core.shared.meta;



public class ConstraintReferential extends Constraint {
    private Column[] columns;
    private Column[] refColumns;

    private Table refTable;

    private String revName;

    private boolean hidden;

    private boolean revHidden;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean p) {
        this.hidden = p;
    }

    public boolean isRevHidden() {
        return revHidden;
    }

    public void setRevHidden(boolean p) {
        this.revHidden = p;
    }

    public String getRevName() {
        return revName;
    }

    public void setRevName(String p) {
        this.revName = p;
    }

    // for GWT RPC
    public ConstraintReferential() {
    }

    public ConstraintReferential(String name, Table t) {
        super(name, t);
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] p) {
        this.columns = p;
    }

    public Column[] getRefColumns() {
        return refColumns;
    }

    public void setRefColumns(Column[] p) {
        this.refColumns = p;
    }

    public Table getRefTable() {
        return refTable;
    }

    public void setRefTable(Table p) {
        this.refTable = p;
    }

    @Override
    public String toString() {
        return "FK:" + getName() + "(" + getTable() + " to " + getRefTable() + ")";
    }
}
