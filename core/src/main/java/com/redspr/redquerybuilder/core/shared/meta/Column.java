package com.redspr.redquerybuilder.core.shared.meta;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Column implements HasLabel, HasStyleName, Serializable, IsSerializable {
    private String name;
    private String label;
    private Type type;
    private Table table;

    private String styleName;

    private boolean hidden;

    private Editor editor;

    public Column() {
    }

    public Column(String name2, Type type2) {
        this.name = name2;
        this.label = name2;
        this.type = type2;
    }

    public Editor getEditor() {
        if (editor == null) {
            return type.getEditor();
        }
        return editor;
    }

    public void setEditor(Editor p) {
        this.editor = p;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean p) {
        this.hidden = p;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setTable(Table p) {
        this.table = p;
    }

    public Table getTable() {
        return table;
    }

    public void setLabel(String p) {
        this.label = p;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public String getStyleName() {
        if (styleName == null) {
            return getType().getStyleName();
        }
        return styleName;
    }

    @Override
    public void setStyleName(String p) {
        this.styleName = p;
    }
}
