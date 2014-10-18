package com.redspr.redquerybuilder.core.shared.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




public class Table extends SchemaObjectBase implements HasLabel {

    private final Map<String, Column> columns = new LinkedHashMap<String, Column>();

    private final List<Constraint> constraints = new ArrayList<Constraint>();

    private boolean hidden;


    public Table() {
    }

    public Table(String name) {
        setObjectName(name);
    }

    public void add(Column c) {
        c.setTable(this);
        columns.put(c.getName().toUpperCase(), c);
    }

    public Collection<Column> getColumns() {
        return columns.values();
    }

    public Column getColumn(String name) {
        return columns.get(name.toUpperCase());
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public Constraint getConstraintByName(String name) {
        for (Constraint c : constraints) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unable to find constraint '" + name + "'");
    }

    public void add(Constraint c) {
        constraints.add(c);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean p) {
        this.hidden = p;
    }

    @Override
    public String toString() {
        return "Table:" + getName();
    }
}
