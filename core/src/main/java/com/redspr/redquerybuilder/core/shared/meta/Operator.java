package com.redspr.redquerybuilder.core.shared.meta;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Operator implements HasLabel, IsSerializable {

    public static final String EQUAL = "=";
    public static final String NOT_EQUAL = "<>";
    public static final String BIGGER = ">";
    public static final String SMALLER = "<";
    public static final String SMALLER_EQUAL = "<=";
    public static final String BIGGER_EQUAL = ">=";
    public static final String IS_NULL = "IS NULL";
    public static final String IS_NOT_NULL = "IS NOT NULL";

    private String name;
    private String label;

    private Cardinality cardinality;

    // For GWT RPC
    private Operator() {
    }

    public Operator(String name2) {
        this(name2, Cardinality.ONE);
    }

    public Operator(String name2, Cardinality c2) {
        this.name = name2;
        this.label = name;
        this.cardinality = c2;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    public String getName() {
        return name;
    }

    public void setLabel(String p) {
        this.label = p;
    }

    public void setCardinality(Cardinality p) {
        this.cardinality = p;
    }
}
