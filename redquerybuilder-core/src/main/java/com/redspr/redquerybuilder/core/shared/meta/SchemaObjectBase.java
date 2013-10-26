package com.redspr.redquerybuilder.core.shared.meta;


/**
 * The base class for classes implementing SchemaObject.
 */
public abstract class SchemaObjectBase extends DbObjectBase implements SchemaObject {

    private Schema schema;

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public String getSQL() {
        if (schema == null) {
            return super.getSQL(); // XXX legit for schema to be null?
        }
        return schema.getSQL() + "." + super.getSQL();
    }
}
