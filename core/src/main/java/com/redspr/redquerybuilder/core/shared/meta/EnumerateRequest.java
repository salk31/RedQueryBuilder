package com.redspr.redquerybuilder.core.shared.meta;

public class EnumerateRequest {
    private String tableName;

    private String columnName;

    private String columnType;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String p) {
        this.tableName = p;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String p) {
        this.columnName = p;
    }

    public String getColumnTypeName() {
        return columnType;
    }

    public void setColumnTypeName(String p) {
        this.columnType = p;
    }

}
