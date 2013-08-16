package com.redspr.redquerybuilder.core.shared.meta;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A database object such as a table, an index, or a user.
 */
public interface DbObject extends Serializable, IsSerializable {

    /**
     * The object is of the type table or view.
     */
    int TABLE_OR_VIEW = 0;

    /**
     * This object is an index.
     */
    int INDEX = 1;

    /**
     * This object is a user.
     */
    int USER = 2;

    /**
     * This object is a sequence.
     */
    int SEQUENCE = 3;

    /**
     * This object is a trigger.
     */
    int TRIGGER = 4;

    /**
     * This object is a constraint (check constraint, unique constraint, or
     * referential constraint).
     */
    int CONSTRAINT = 5;

    /**
     * This object is a setting.
     */
    int SETTING = 6;

    /**
     * This object is a role.
     */
    int ROLE = 7;

    /**
     * This object is a right.
     */
    int RIGHT = 8;

    /**
     * This object is an alias for a Java function.
     */
    int FUNCTION_ALIAS = 9;

    /**
     * This object is a schema.
     */
    int SCHEMA = 10;

    /**
     * This object is a constant.
     */
    int CONSTANT = 11;

    /**
     * This object is a user data type (domain).
     */
    int USER_DATATYPE = 12;

    /**
     * This object is a comment.
     */
    int COMMENT = 13;

    /**
     * This object is a user-defined aggregate function.
     */
    int AGGREGATE = 14;



    /**
     * Get the SQL name of this object (may be quoted).
     *
     * @return the SQL name
     */
    String getSQL();

    /**
     * Get the name.
     *
     * @return the name
     */
    String getName();


}
