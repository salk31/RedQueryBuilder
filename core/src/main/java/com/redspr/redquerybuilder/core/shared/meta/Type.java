package com.redspr.redquerybuilder.core.shared.meta;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Type implements HasStyleName, Serializable, IsSerializable {
    private String name;

    private Operator[] operators = new Operator[]{new Operator("="), new Operator("IS NULL", Cardinality.ZERO), new Operator("LIKE")};

    private Editor editor;

    private String styleName;

    // for GWT RPC
    private Type() {
    }

    public Type(String name2) {
        this(name2, new Editor.TextEditor());
    }

    public Type(String name2, Editor editor2) {
        this.name = name2;
        this.editor = editor2;
    }

    public Operator[] getOperators() {
        return operators;
    }
    public void setOperators(Operator[] operators) {
        this.operators = operators;
    }
    public String getName() {
        return name;
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(Editor p) {
        this.editor = p;
    }

    public Operator getOperatorByName(String opName) {
        for (Operator op : operators) {
            if (opName.equals(op.getName())) {
                return op;
            }
        }
        return null;
    }

    public Operator getOperatorByNameOrFail(String opName) {
        Operator op = getOperatorByName(opName);
        if (op == null) {
            throw new RuntimeException("Could not find operator '" + opName
                    + "' in type '" + name + "'");
        }
        return op;
    }

    public String getStyleName() {
        if (styleName == null) {
            return getEditor().getStyleName();
        }
        return styleName;
    }

    @Override
    public void setStyleName(String p) {
        this.styleName = p;
    }
}
