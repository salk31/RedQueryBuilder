package com.redspr.redquerybuilder.core.shared.meta;


/**
 * Any database object that is stored in a schema.
 */
public interface SchemaObject extends DbObject {

    /**
     * Get the schema in which this object is defined
     *
     * @return the schema
     */
    Schema getSchema();
}
