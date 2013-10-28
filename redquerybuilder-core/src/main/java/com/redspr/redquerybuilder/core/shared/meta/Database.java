package com.redspr.redquerybuilder.core.shared.meta;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * There is one database object per open database.
 *
 * The format of the meta data table is:
 *  id int, headPos int (for indexes), objectType int, sql varchar
 *
 * @since 2004-04-15 22:49
 */
public class Database implements Serializable, IsSerializable {

    private String databaseName;
    private String databaseShortName;

    private Schema mainSchema;

    private Type[] types;


    public Database() {
        this(new Schema());
    }

    public Database(Schema s) {
        mainSchema = s;
    }

    public Type[] getTypes() {
        return types;
    }

    public void setTypes(Type[] p) {
        this.types = p;
    }

    public Type getTypeByName(String typeName) {
        for (Type t : types) {
            if (typeName.equals(t.getName())) {
                return t;
            }
        }
        throw new RuntimeException("Could not find type '" + typeName + "'");
    }

    public Operator getOperatorByName(String name) {
        for (Type type: types) {
            for (Operator op : type.getOperators()) {
                if (name.equals(op.getName())) {
                    return op;
                }
            }
        }

        return null;
    }

    public Schema getMainSchema() {
        return mainSchema;
    }

    public String getName() {
        return databaseName;
    }

    @Override
    public String toString() {
        return databaseShortName + ":" + super.toString();
    }
}
