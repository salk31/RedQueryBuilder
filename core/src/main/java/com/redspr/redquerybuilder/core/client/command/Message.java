/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 *
 * Nicolas Fortin, Atelier SIG, IRSTV FR CNRS 24888
 * Support for the operator "&&" as an alias for SPATIAL_INTERSECTS
 */
package com.redspr.redquerybuilder.core.client.command;

import java.sql.SQLException;

public class Message {
    public static SQLException addSQL(Exception e, String sql) {
        return new SQLException(sql, e);
    }

    public static SQLException getSyntaxError(String sqlCommand, int parseIndex) {
        return new SQLException("In " + sqlCommand + " at " + parseIndex + " wanted ");
    }

    public static SQLException getSyntaxError(String sqlCommand, int parseIndex, String expected) {
        return new SQLException("In " + sqlCommand + " at " + parseIndex + " wanted " + expected);
    }

    public static SQLException getSQLException(int code, Exception e, String expected) {
        return new SQLException("In " + code + " at " + e + " wanted " + expected, e);
    }

    public static SQLException getSQLException(int code) {
        return new SQLException("In " + code);
    }
    public static SQLException getSQLException(int code, String... msg) {
        return new SQLException("In " + code);
    }

    public static RuntimeException throwInternalError(String msg) {
        return new RuntimeException(msg);
    }
}
