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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.redspr.redquerybuilder.core.client.command.dml.Query;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.command.dml.SelectUnion;
import com.redspr.redquerybuilder.core.client.constant.ErrorCode;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.expression.Comparison;
import com.redspr.redquerybuilder.core.client.expression.ConditionAndOr;
import com.redspr.redquerybuilder.core.client.expression.Expression;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.client.expression.Null;
import com.redspr.redquerybuilder.core.client.expression.Parameter;
import com.redspr.redquerybuilder.core.client.expression.Wildcard;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.client.util.StatementBuilder;
import com.redspr.redquerybuilder.core.client.value.Value;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Database;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.Schema;
import com.redspr.redquerybuilder.core.shared.meta.Table;


/**
 * The parser is used to convert a SQL statement string to an command object.
 */
public class Parser {

    // used during the tokenizer phase
    private static final int CHAR_END = -1, CHAR_VALUE = 2, CHAR_QUOTED = 3;
    private static final int CHAR_NAME = 4, CHAR_SPECIAL_1 = 5, CHAR_SPECIAL_2 = 6;
    private static final int CHAR_STRING = 7, CHAR_DECIMAL = 8, CHAR_DOLLAR_QUOTED_STRING = 9;

    // this are token types
    private static final int KEYWORD = 1, IDENTIFIER = 2, PARAMETER = 3, END = 4, VALUE = 5;
    private static final int EQUAL = 6, BIGGER_EQUAL = 7, BIGGER = 8;
    private static final int SMALLER = 9, SMALLER_EQUAL = 10, NOT_EQUAL = 11, AT = 12;
    private static final int MINUS = 17, PLUS = 18;
    private static final int STRING_CONCAT = 22;
    private static final int OPEN = 31, CLOSE = 32, NULL = 34, TRUE = 40, FALSE = 41;

    private static final int CURRENT_TIMESTAMP = 42, CURRENT_DATE = 43, CURRENT_TIME = 44, ROWNUM = 45;

    private final Database database;
    private final  Session session;

    private int[] characterTypes;
    private int currentTokenType;
    private String currentToken;
    private boolean currentTokenQuoted;
    private Value currentValue;
    private String sqlCommand;
    private String originalSQL;
    private char[] sqlCommandChars;
    private int lastParseIndex;
    private int parseIndex;
    //private CreateView createView;
    private Prepared currentPrepared;
    private Select currentSelect;
    private ObjectArray<Parameter> parameters;
    private String schemaName;
    private ObjectArray<String> expectedList;
    private boolean rightsChecked;
    private boolean recompileAlways;
    private ObjectArray<Parameter> indexedParameterList;

    public Parser(Session session2) {
        this.session = session2;
        database = session.getDatabase();
    }


    /**
     * Parse the statement, but don't prepare it for execution.
     *
     * @param sql the SQL statement to parse
     * @return the prepared object
     */
    public Prepared parseOnly(String sql) throws SQLException {
        return parse(sql);
    }



    private Prepared parse(String sql) throws SQLException {
        Prepared p = null;
       // try {
            // first, try the fast variant
            p = parse(sql, false);
//        }// catch (SQLException e) {
//            if (e.getErrorCode() == ErrorCode.SYNTAX_ERROR_1) {
//                // now, get the detailed exception
//                p = parse(sql, true);
//            } else {
//               throw Message.addSQL(e, sql);
//            }
        //}
       // p.setPrepareAlways(recompileAlways);
       // p.setParameterList(parameters);
        return p;
    }

    private Prepared parse(String sql, boolean withExpectedList) throws SQLException {
        initialize(sql);
        if (withExpectedList) {
            //expectedList = ObjectArray.newInstance();
        } else {
           // expectedList = null;
        }
        parameters = ObjectArray.newInstance();
        currentSelect = null;
        currentPrepared = null;
        //createView = null;
        recompileAlways = false;
        //indexedParameterList = null;
        read();
        return parsePrepared();
    }

    private Prepared parsePrepared() throws SQLException {
        int start = lastParseIndex;
        Prepared c = null;
        String token = currentToken;
        if (token.length() == 0) {
            c = new NoOperation();
        } else {
            char first = token.charAt(0);
            switch (first) {
            case '(':
                c = parseSelect();
                break;
            case 'A':
                if (readIf("ALTER")) {
                    c = parseAlter();
                } else if (readIf("ANALYZE")) {
                    c = parseAnalyze();
                }
                break;
            case 'F':
                if (isToken("FROM")) {
                    c = parseSelect();
                }
                break;
            case 'I':
                if (readIf("INSERT")) {
                    c = parseInsert();
                }
                break;
            case 'M':
                if (readIf("MERGE")) {
                    c = parseMerge();
                }
                break;
            case 'P':
                if (readIf("PREPARE")) {
                    c = parsePrepare();
                }
                break;
            case 'S':
                if (isToken("SELECT")) {
                    c = parseSelect();
                } else if (readIf("SET")) {
                    c = parseSet();
                } else if (readIf("SAVEPOINT")) {
                    c = parseSavepoint();
                } else if (readIf("SCRIPT")) {
                    c = parseScript();
                } else if (readIf("SHUTDOWN")) {
                    c = parseShutdown();
                } else if (readIf("SHOW")) {
                    c = parseShow();
                }
                break;
            case 'T':
                if (readIf("TRUNCATE")) {
                    c = parseTruncate();
                }
                break;
            case 'U':
                if (readIf("UPDATE")) {
                    c = parseUpdate();
                }
                break;
            case 'V':
                if (readIf("VALUES")) {
                    c = parserCall();
                }
                break;
            case 'W':
                if (readIf("WITH")) {
                    c = parserWith();
                }
                break;
            default:
                throw getSyntaxError();
            }
            if (indexedParameterList != null) {
                for (int i = 0; i < indexedParameterList.size(); i++) {
                    if (indexedParameterList.get(i) == null) {
                        indexedParameterList.set(i, new Parameter(session, i));
                    }
                }
                parameters = indexedParameterList;
            }
            if (readIf("{")) {
                do {
                    int index = (int) readLong() - 1;
                    if (index < 0 || index >= parameters.size()) {
                        throw getSyntaxError();
                    }
                    Parameter p = parameters.get(index);
                    if (p == null) {
                        throw getSyntaxError();
                    }
                    read(":");
                    Expression expr = readExpression();
                  //  expr = expr.optimize(session);
                    //p.setValue(expr.getValue(session));
                } while (readIf(","));
                read("}");
                for (Parameter p : parameters) {
                    //p.checkSet();
                }
                parameters.clear();
            }
        }
        if (c == null) {
            throw getSyntaxError();
        }
        setSQL(c, null, start);
        return c;
    }

    private SQLException getSyntaxError() {
        if (expectedList == null || expectedList.size() == 0) {
            return Message.getSyntaxError(sqlCommand, parseIndex);
        }
        StatementBuilder buff = new StatementBuilder();
        for (String e : expectedList) {
            buff.appendExceptFirst(", ");
            buff.append(e);
        }
        return Message.getSyntaxError(sqlCommand, parseIndex, buff.toString());
    }

    private Prepared parseAnalyze() throws SQLException {
//        Analyze command = new Analyze(session);
//        if (readIf("SAMPLE_SIZE")) {
//            command.setTop(getPositiveInt());
//        }
//        return command;
        return null;
    }

    private Prepared parseShutdown() throws SQLException {
        return null;
    }

    private Prepared parsePrepare() throws SQLException {
        return null;
    }

    private Prepared parseSavepoint() throws SQLException {
        return null;
    }

    private Schema getSchema() throws SQLException {
//        if (schemaName == null) {
//            return null;
//        }
//        Schema schema = database.findSchema(schemaName);
//        if (schema == null) {
//            if ("SESSION".equals(schemaName)) {
//                // for local temporary tables
//                schema = database.getSchema(session.getCurrentSchemaName());
//            } else {
//                throw Message.getSQLException(ErrorCode.SCHEMA_NOT_FOUND_1, schemaName);
//            }
//        }
//        return schema;
        return null;
    }

    private Column readTableColumn(TableFilter filter) throws SQLException {
//        String tableAlias = null;
//        String columnName = readColumnIdentifier();
//        if (readIf(".")) {
//            tableAlias = columnName;
//            columnName = readColumnIdentifier();
//            if (readIf(".")) {
//                String schema = tableAlias;
//                tableAlias = columnName;
//                columnName = readColumnIdentifier();
//                if (readIf(".")) {
//                    String catalogName = schema;
//                    schema = tableAlias;
//                    tableAlias = columnName;
//                    columnName = readColumnIdentifier();
//                    if (!catalogName.equals(database.getShortName())) {
//                        throw Message.getSQLException(ErrorCode.DATABASE_NOT_FOUND_1, catalogName);
//                    }
//                }
//                if (!schema.equals(filter.getTable().getSchema().getName())) {
//                    throw Message.getSQLException(ErrorCode.SCHEMA_NOT_FOUND_1, schema);
//                }
//            }
//            if (!tableAlias.equals(filter.getTableAlias())) {
//                throw Message.getSQLException(ErrorCode.TABLE_OR_VIEW_NOT_FOUND_1, tableAlias);
//            }
//        }
//        return filter.getTable().getColumn(columnName);
        return null;
    }

    private Prepared parseUpdate() throws SQLException {
        return null;
    }

    private TableFilter readSimpleTableFilter() throws SQLException {
//        Table table = readTableOrView();
//        String alias = null;
//        if (readIf("AS")) {
//            alias = readAliasIdentifier();
//        } else if (currentTokenType == IDENTIFIER) {
//            if (!"SET".equals(currentToken)) {
//                // SET is not a keyword (PostgreSQL supports it as a table name)
//                alias = readAliasIdentifier();
//            }
//        }
//        return new TableFilter(session, table, alias, rightsChecked, currentSelect);
        return null;
    }

    private IndexColumn[] parseIndexColumnList() throws SQLException {
//        ObjectArray<IndexColumn> columns = ObjectArray.newInstance();
//        do {
//            IndexColumn column = new IndexColumn();
//            column.columnName = readColumnIdentifier();
//            columns.add(column);
//            if (readIf("ASC")) {
//                // ignore
//            } else if (readIf("DESC")) {
//                column.sortType = SortOrder.DESCENDING;
//            }
//            if (readIf("NULLS")) {
//                if (readIf("FIRST")) {
//                    column.sortType |= SortOrder.NULLS_FIRST;
//                } else {
//                    read("LAST");
//                    column.sortType |= SortOrder.NULLS_LAST;
//                }
//            }
//        } while (readIf(","));
//        read(")");
//        return columns.toArray(new IndexColumn[columns.size()]);
        return null;
    }

    private String[] parseColumnList() throws SQLException {
        ObjectArray<String> columns = ObjectArray.newInstance();
        do {
            String columnName = readColumnIdentifier();
            columns.add(columnName);
        } while (readIfMore());
        return columns.toArray(new String[columns.size()]);
    }

    private Column[] parseColumnList(Table table) throws SQLException {
//        ObjectArray<Column> columns = ObjectArray.newInstance();
//        HashSet<Column> set = New.hashSet();
//        if (!readIf(")")) {
//            do {
//                Column column = table.getColumn(readColumnIdentifier());
//                if (!set.add(column)) {
//                    throw Message.getSQLException(ErrorCode.DUPLICATE_COLUMN_NAME_1, column.getSQL());
//                }
//                columns.add(column);
//            } while (readIfMore());
//        }
//        return columns.toArray(new Column[columns.size()]);
        return null;
    }

    private boolean readIfMore() throws SQLException {
        if (readIf(",")) {
            return !readIf(")");
        }
        read(")");
        return false;
    }

    private Prepared parseHelp() throws SQLException {
        return null;
    }

    private Prepared parseShow() throws SQLException {
        return null;
//        ObjectArray<Value> paramValues = ObjectArray.newInstance();
//        StringBuilder buff = new StringBuilder("SELECT ");
//        if (readIf("CLIENT_ENCODING")) {
//            // for PostgreSQL compatibility
//            buff.append("'UNICODE' AS CLIENT_ENCODING FROM DUAL");
//        } else if (readIf("DATESTYLE")) {
//            // for PostgreSQL compatibility
//            buff.append("'ISO' AS DATESTYLE FROM DUAL");
//        } else if (readIf("SERVER_VERSION")) {
//            // for PostgreSQL compatibility
//            buff.append("'8.1.4' AS SERVER_VERSION FROM DUAL");
//        } else if (readIf("SERVER_ENCODING")) {
//            // for PostgreSQL compatibility
//            buff.append("'UTF8' AS SERVER_ENCODING FROM DUAL");
//        } else if (readIf("TABLES")) {
//            // for MySQL compatibility
//            String schema = Constants.SCHEMA_MAIN;
//            if (readIf("FROM")) {
//                schema = readUniqueIdentifier();
//            }
//            buff.append("TABLE_NAME, TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=? ORDER BY TABLE_NAME");
//            paramValues.add(ValueString.get(schema));
//        } else if (readIf("COLUMNS")) {
//            // for MySQL compatibility
//            read("FROM");
//            String tableName = readUniqueIdentifier();
//            paramValues.add(ValueString.get(tableName));
//            String schema = Constants.SCHEMA_MAIN;
//            if (readIf("FROM")) {
//                schema = readUniqueIdentifier();
//            }
//            buff.append("C.COLUMN_NAME FIELD, " +
//                    "C.TYPE_NAME || '(' || C.NUMERIC_PRECISION || ')' TYPE, " +
//                    "C.IS_NULLABLE \"NULL\", " +
//                    "CASE I.INDEX_TYPE_NAME WHEN 'PRIMARY KEY' THEN 'PRI' WHEN 'UNIQUE INDEX' THEN 'UNI' ELSE '' END KEY, " +
//                    "IFNULL(COLUMN_DEFAULT, 'NULL') DEFAULT " +
//                    "FROM INFORMATION_SCHEMA.COLUMNS C LEFT OUTER JOIN INFORMATION_SCHEMA.INDEXES I " +
//                    "ON I.TABLE_SCHEMA=C.TABLE_SCHEMA " +
//                    "AND I.TABLE_NAME=C.TABLE_NAME " +
//                    "AND I.COLUMN_NAME=C.COLUMN_NAME " +
//                    "WHERE C.TABLE_NAME=? AND C.TABLE_SCHEMA=? " +
//                    "ORDER BY C.ORDINAL_POSITION");
//            //paramValues.add(ValueString.get(schema));
//        } else if (readIf("DATABASES") || readIf("SCHEMAS")) {
//            // for MySQL compatibility
//            buff.append("SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA");
//        }
//        return prepare(session, buff.toString(), paramValues);
    }



    private Prepared parseMerge() throws SQLException {
        return null;
    }

    private Prepared parseInsert() throws SQLException {
       return null;
    }

    private TableFilter readTableFilter(boolean fromOuter) throws SQLException {
        Table table;
        String alias = null;
//        if (readIf("(")) {
//            if (isToken("SELECT") || isToken("FROM")) {
//                int start = lastParseIndex;
//                int paramIndex = parameters.size();
//                Query query = parseSelectUnion();
//                read(")");
//                query = parseSelectUnionExtension(query, start, true);
//                ObjectArray<Parameter> params = ObjectArray.newInstance();
//                for (int i = paramIndex; i < parameters.size(); i++) {
//                    params.add(parameters.get(i));
//                }
//                query.setParameterList(params);
//                query.init();
//                Session s;
//                if (createView != null) {
//                    s = database.getSystemSession();
//                } else {
//                    s = session;
//                }
//                alias = session.getNextSystemIdentifier(sqlCommand);
//                table = TableView.createTempView(s, session.getUser(), alias, query, currentSelect);
//            } else {
//                TableFilter top = readTableFilter(fromOuter);
//                top = readJoin(top, currentSelect, fromOuter);
//                read(")");
//                alias = readFromAlias(null);
//                if (alias != null) {
//                    top.setAlias(alias);
//                }
//                return top;
//            }
//        } else {
            String tableName = readIdentifierWithSchema(null);
//            if (readIf("(")) {
//                Schema mainSchema = database.getSchema(Constants.SCHEMA_MAIN);
//                if (tableName.equals(RangeTable.NAME)) {
//                    Expression min = readExpression();
//                    read(",");
//                    Expression max = readExpression();
//                    read(")");
//                    table = new RangeTable(mainSchema, min, max);
//                } else {
//                    Expression func = readFunction(tableName);
//                    if (!(func instanceof FunctionCall)) {
//                        throw getSyntaxError();
//                    }
//                    table = new FunctionTable(mainSchema, session, func, (FunctionCall) func);
//                }
//            } else if ("DUAL".equals(tableName)) {
//                table = getDualTable();
//            } else {
               table = readTableOrView(tableName);
//            }
//        }
        alias = readFromAlias(alias);
        return new TableFilter(session, table, alias, currentSelect);

    }

    private String readFromAlias(String alias) throws SQLException {
        if (readIf("AS")) {
            alias = readAliasIdentifier();
        } else if (currentTokenType == IDENTIFIER) {
            // left and right are not keywords (because they are functions as
            // well)
            if (!isToken("LEFT") && !isToken("RIGHT") && !isToken("FULL")) {
                alias = readAliasIdentifier();
            }
        }
        return alias;
    }

    private Prepared parseTruncate() throws SQLException {
        return null;
    }

    private boolean readIfExists(boolean ifExists) throws SQLException {
        if (readIf("IF")) {
            read("EXISTS");
            ifExists = true;
        }
        return ifExists;
    }

    private Prepared parseComment() throws SQLException {
       return null;
    }

    private Prepared parseDropUserDataType() throws SQLException {
        return null;
    }

    private Prepared parseDropAggregate() throws SQLException {
        return null;
    }

    private TableFilter readJoin(TableFilter top, Select command, boolean fromOuter) throws SQLException {
        TableFilter last = top;
        while (true) {
            if (readIf("RIGHT")) {
                readIf("OUTER");
                read("JOIN");
                // the right hand side is the 'inner' table usually
                TableFilter newTop = readTableFilter(fromOuter);
                newTop = readJoin(newTop, command, true);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                newTop.addJoin(top, true, on);
                top = newTop;
                last = newTop;
            } else if (readIf("LEFT")) {
                readIf("OUTER");
                read("JOIN");
                TableFilter join = readTableFilter(true);
                top = readJoin(top, command, true);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                top.addJoin(join, true, on);
                last = join;
            } else if (readIf("FULL")) {
                throw this.getSyntaxError();
            } else if (readIf("INNER")) {
                read("JOIN");
                TableFilter join = readTableFilter(fromOuter);
                top = readJoin(top, command, false);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                top.addJoin(join, fromOuter, on);
                last = join;
            } else if (readIf("JOIN")) {
                TableFilter join = readTableFilter(fromOuter);
                top = readJoin(top, command, false);
                Expression on = null;
                if (readIf("ON")) {
                    on = readExpression();
                }
                top.addJoin(join, fromOuter, on);
                last = join;
            } else if (readIf("CROSS")) {
                read("JOIN");
                TableFilter join = readTableFilter(fromOuter);
                top.addJoin(join, fromOuter, null);
                last = join;
            } else if (readIf("NATURAL")) {
                read("JOIN");
                TableFilter join = readTableFilter(fromOuter);
                Column[] tableCols = last.getTable().getColumns().toArray(new Column[0]);
                Column[] joinCols = join.getTable().getColumns().toArray(new Column[0]);
                String tableSchema = last.getTable().getSchema().getName();
                String joinSchema = join.getTable().getSchema().getName();
                Expression on = null;
                for (Column tc : tableCols) {
                    String tableColumnName = tc.getName();
                    for (Column c : joinCols) {
                        String joinColumnName = c.getName();
                        if (tableColumnName.equals(joinColumnName)) {
                            // XXX join.addNaturalJoinColumn(c);
                            Expression tableExpr = new ExpressionColumn(session, tableSchema, last
                                    .getTableAlias(), tableColumnName);
                            Expression joinExpr = new ExpressionColumn(session, joinSchema, join
                                    .getTableAlias(), joinColumnName);
                            Expression equal = new Comparison(session, Operator.EQUAL, tableExpr, joinExpr);
                            if (on == null) {
                                on = equal;
                            } else {
                                on = new ConditionAndOr(session, ConditionAndOr.AND, on, equal);
                            }
                        }
                    }
                }
                top.addJoin(join, fromOuter, on);
                last = join;
            } else {
                break;
            }
        }
        return top;
    }

    private Query parseSelect() throws SQLException {
      //  int paramIndex = parameters.size();
        Query command = parseSelectUnion();
//        ObjectArray<Parameter> params = ObjectArray.newInstance();
//        for (int i = paramIndex; i < parameters.size(); i++) {
//            params.add(parameters.get(i));
//        }
//        command.setParameterList(params);
//        command.init();

        return command;
    }

    private Query parseSelectUnion() throws SQLException {
        int start = lastParseIndex;
        Query command = parseSelectSub();
        return parseSelectUnionExtension(command, start, false);
    }

    private Query parseSelectUnionExtension(Query command, int start, boolean unionOnly) throws SQLException {
          while (true) {
            if (readIf("UNION")) {
                SelectUnion union = new SelectUnion(session, command);
                if (readIf("ALL")) {
                    union.setUnionType(SelectUnion.UNION_ALL);
                } else {
                    readIf("DISTINCT");
                    union.setUnionType(SelectUnion.UNION);
                }
                union.setRight(parseSelectSub());
                command = union;
            } else if (readIf("MINUS") || readIf("EXCEPT")) {
                SelectUnion union = new SelectUnion(session, command);
                union.setUnionType(SelectUnion.EXCEPT);
                union.setRight(parseSelectSub());
                command = union;
            } else if (readIf("INTERSECT")) {
                SelectUnion union = new SelectUnion(session, command);
                union.setUnionType(SelectUnion.INTERSECT);
                union.setRight(parseSelectSub());
                command = union;
            } else {
                break;
            }
        }
        if (!unionOnly) {
            parseEndOfQuery(command);
        }
        setSQL(command, null, start);
        return command;
    }

    private void parseEndOfQuery(Prepared command) throws SQLException {
//
//        if (readIf("ORDER")) {
//            read("BY");
//            Select oldSelect = currentSelect;
//            if (command instanceof Select) {
//                currentSelect = (Select) command;
//            }
//            ObjectArray<SelectOrderBy> orderList = ObjectArray.newInstance();
//            do {
//                boolean canBeNumber = true;
//                if (readIf("=")) {
//                    canBeNumber = false;
//                }
//                SelectOrderBy order = new SelectOrderBy();
//                Expression expr = readExpression();
//                if (canBeNumber && expr instanceof ValueExpression && expr.getType() == Value.INT) {
//                    order.columnIndexExpr = expr;
//                } else if (expr instanceof Parameter) {
//                    recompileAlways = true;
//                    order.columnIndexExpr = expr;
//                } else {
//                    order.expression = expr;
//                }
//                if (readIf("DESC")) {
//                    order.descending = true;
//                } else {
//                    readIf("ASC");
//                }
//                if (readIf("NULLS")) {
//                    if (readIf("FIRST")) {
//                        order.nullsFirst = true;
//                    } else {
//                        read("LAST");
//                        order.nullsLast = true;
//                    }
//                }
//                orderList.add(order);
//            } while (readIf(","));
//            command.setOrder(orderList);
//            currentSelect = oldSelect;
//        }
//        if (database.getMode().supportOffsetFetch) {
//            // make sure aggregate functions will not work here
//            Select temp = currentSelect;
//            currentSelect = null;
//
//            // http://sqlpro.developpez.com/SQL2008/
//            if (readIf("OFFSET")) {
//                command.setOffset(readExpression().optimize(session));
//                if (!readIf("ROW")) {
//                    read("ROWS");
//                }
//            }
//            if (readIf("FETCH")) {
//                read("FIRST");
//                if (readIf("ROW")) {
//                    command.setLimit(ValueExpression.get(ValueInt.get(1)));
//                } else {
//                    Expression limit = readExpression().optimize(session);
//                    command.setLimit(limit);
//                    if (!readIf("ROW")) {
//                        read("ROWS");
//                    }
//                }
//                read("ONLY");
//            }
//
//            currentSelect = temp;
//        }
//        if (readIf("LIMIT")) {
//            Select temp = currentSelect;
//            // make sure aggregate functions will not work here
//            currentSelect = null;
//            Expression limit = readExpression().optimize(session);
//            command.setLimit(limit);
//            if (readIf("OFFSET")) {
//                Expression offset = readExpression().optimize(session);
//                command.setOffset(offset);
//            } else if (readIf(",")) {
//                // MySQL: [offset, ] rowcount
//                Expression offset = limit;
//                limit = readExpression().optimize(session);
//                command.setOffset(offset);
//                command.setLimit(limit);
//            }
//            if (readIf("SAMPLE_SIZE")) {
//                command.setSampleSize(getPositiveInt());
//            }
//            currentSelect = temp;
//        }
//        if (readIf("FOR")) {
//            if (readIf("UPDATE")) {
//                if (readIf("OF")) {
//                    do {
//                        readIdentifierWithSchema();
//                    } while (readIf(","));
//                } else if (readIf("NOWAIT")) {
//                    // TOxDO parser: select for update nowait: should not wait
//                } else if (readIf("WITH")) {
//                    // Hibernate / Derby support
//                    read("RR");
//                }
//                command.setForUpdate(true);
//            } else if (readIf("READ")) {
//                read("ONLY");
//                if (readIf("WITH")) {
//                    read("RS");
//                }
//            }
//        }
    }

    private Query parseSelectSub() throws SQLException {
        if (readIf("(")) {
           Query command = parseSelectUnion();
            read(")");
            return command;
        }
       Select select = parseSelectSimple();
       return select;
    }

    private void parseSelectSimpleFromPart(Select command) throws SQLException {
        do {
            TableFilter filter = readTableFilter(false);
            parseJoinTableFilter(filter, command);
        } while (readIf(","));
    }

    private void parseJoinTableFilter(TableFilter top, Select command) throws SQLException {
        top = readJoin(top, command, top.isJoinOuter());
        command.addTableFilter(top, true);
        boolean isOuter = false;
        while (true) {
            TableFilter join = top.getJoin();
            if (join == null) {
                break;
            }
            isOuter = isOuter | join.isJoinOuter();
            if (isOuter) {
                command.addTableFilter(join, false);
            } else {
                // make flat so the optimizer can work better
                //Expression on = join.getJoinCondition();
                //if (on != null) {
                //    command.addCondition(on);
                //}
                //join.removeJoinCondition();
                // top.removeJoin();
                command.addTableFilter(join, true);
            }
            top = join;
        }
    }

    private void parseSelectSimpleSelectPart(Select command) throws SQLException {
        Select temp = currentSelect;
        // make sure aggregate functions will not work in TOP and LIMIT
        currentSelect = null;
        if (readIf("TOP")) {
            // can't read more complex expressions here because
            // SELECT TOP 1 +? A FROM TEST could mean
            // SELECT TOP (1+?) A FROM TEST or
            // SELECT TOP 1 (+?) AS A FROM TEST
           // Expression limit = readTerm().optimize(session);
           // command.setLimit(limit);
        } else if (readIf("LIMIT")) {
           // Expression offset = readTerm().optimize(session);
           // command.setOffset(offset);
           // Expression limit = readTerm().optimize(session);
           // command.setLimit(limit);
        }
        currentSelect = temp;
        if (readIf("DISTINCT")) {
           // command.setDistinct(true);
        } else {
            readIf("ALL");
        }
        ObjectArray<Expression> expressions = ObjectArray.newInstance();
        do {
            if (readIf("*")) {
                expressions.add(new Wildcard(session, null, null));
            } else {
                Expression expr = readExpression();
                if (readIf("AS") || currentTokenType == IDENTIFIER) {
                    String alias = readAliasIdentifier();
          //          expr = new Alias(expr, alias, database.getMode().aliasColumnName);
                }
                expressions.add(expr);
            }
        } while (readIf(","));
        command.setExpressions(expressions);
    }

    private Select parseSelectSimple() throws SQLException {
        boolean fromFirst;
        if (readIf("SELECT")) {
            fromFirst = false;
        } else if (readIf("FROM")) {
            fromFirst = true;
        } else {
            throw getSyntaxError();
        }

        Select command = new Select(session);
        int start = lastParseIndex;
        Select oldSelect = currentSelect;
        currentSelect = command;
        currentPrepared = command;
        if (fromFirst) {
            parseSelectSimpleFromPart(command);
            read("SELECT");
            parseSelectSimpleSelectPart(command);
        } else {
            parseSelectSimpleSelectPart(command);
            if (!readIf("FROM")) {
                // select without FROM: convert to SELECT ... FROM
                // SYSTEM_RANGE(1,1)
                Table dual = getDualTable();
                TableFilter filter = new TableFilter(session, dual, null, currentSelect);
                command.addTableFilter(filter, true);
            } else {
              //  Window.alert("parseSelectSimple D");
                parseSelectSimpleFromPart(command);
              //  Window.alert("parseSelectSimple E");
            }
        }
     //   Window.alert("parseSelectSimple F" + command);
        if (readIf("WHERE")) {
            Expression condition = readExpression();
            command.addCondition(condition);
        }
     //   Window.alert("parseSelectSimple G" + command);
        // the group by is read for the outer select (or not a select)
        // so that columns that are not grouped can be used
        currentSelect = oldSelect;
        if (readIf("GROUP")) {
            read("BY");
            ObjectArray<Expression> list = ObjectArray.newInstance();
            do {
                Expression expr = readExpression();
                list.add(expr);
            } while (readIf(","));
            command.setGroupBy(list);
        }
        currentSelect = command;
        if (readIf("HAVING")) {
            Expression condition = readExpression();
            command.setHaving(condition);
        }
        // TODO 97 command.setParameterList(parameters);
        currentSelect = oldSelect;
        setSQL(command, "SELECT", start);
        return command;
    }

    private Table getDualTable() throws SQLException {
        return null;
//        Schema main = database.findSchema(Constants.SCHEMA_MAIN);
//        Expression one = ValueExpression.get(ValueLong.get(1));
//        return new RangeTable(main, one, one);
    }

    private void setSQL(Prepared command, String start, int startIndex) {
        String sql = originalSQL.substring(startIndex, lastParseIndex).trim();
        if (start != null) {
            sql = start + " " + sql;
        }
        //command.setSQL(sql);
    }

    private Expression readExpression() throws SQLException {
        Expression r = readAnd();
        while (readIf("OR")) {
            r = new ConditionAndOr(session, ConditionAndOr.OR, r, readAnd());
        }
        return r;
    }

    private Expression readAnd() throws SQLException {
        Expression r = readCondition();
        while (readIf("AND")) {
            r = new ConditionAndOr(session, ConditionAndOr.AND, r, readCondition());
        }
        return r;
    }

    private Expression readCondition() throws SQLException {
//        // TOxDO parser: should probably use switch case for performance
//        if (readIf("NOT")) {
//            return new ConditionNot(readCondition());
//        }
//        if (readIf("EXISTS")) {
//            read("(");
//            Query query = parseSelect();
//            // can not reduce expression because it might be a union except
//            // query with distinct
//            read(")");
//            return new ConditionExists(query);
//        }
        Expression r = readConcat();
        while (true) {
//            // special case: NOT NULL is not part of an expression (as in CREATE
//            // TABLE TEST(ID INT DEFAULT 0 NOT NULL))
//            int backup = parseIndex;
            boolean not = false;
            if (readIf("NOT")) {
                not = true;
//                if (isToken("NULL")) {
//                    // this really only works for NOT NULL!
//                    parseIndex = backup;
//                    currentToken = "NOT";
//                    break;
//                }
            }
            Operator op = readCustom(not);
            if (op != null) {
                Expression b;
                switch (op.getCardinality()) {
                case ZERO: {
                    b = null;
                    break;
                }
                case ONE: {
                    b = readConcat();
                    break;
                }
                case MULTI: {
                    read("(");
                  Collection<Expression> v = new ArrayList<Expression>();
                  Expression last;
                  do {
                      last = readExpression();
                      v.add(last);
                  } while (readIf(","));
                  read(")");
                  b = new Parameter(session, v);
                  break;
                }
                default:
                    throw new IllegalArgumentException("Can't handle "
                            + op.getCardinality());
                }

                Expression esc = null;
                if (readIf("ESCAPE")) {
                    esc = readConcat();
                }
                recompileAlways = true;
                r = new Comparison(session, op.getName(), r, b);
            } //else if (readIf("REGEXP")) {
//                Expression b = readConcat();
//                r = new CompareLike(database.getCompareMode(), r, b, null, true);
           // } else
              if (readIf("IS")) {
                String type;
                if (readIf("NOT")) {
                    type = Operator.IS_NOT_NULL;
                } else {
                    type = Operator.IS_NULL;
                }
                read("NULL");
                r = new Comparison(session, type, r, null);
//            } else if (readIf("IN")) {
//                if (SysProperties.OPTIMIZE_IN && !SysProperties.OPTIMIZE_IN_LIST) {
//                    recompileAlways = true;
//                }
//                read("(");
//                if (readIf(")")) {
//                    r = ValueExpression.get(ValueBoolean.get(false));
//                } else {
//                    if (isToken("SELECT") || isToken("FROM")) {
//                        Query query = parseSelect();
//                        r = new ConditionInSelect(database, r, query, false, Comparison.EQUAL);
//                    } else {
//                        ObjectArray<Expression> v = ObjectArray.newInstance();
//                        Expression last;
//                        do {
//                            last = readExpression();
//                            v.add(last);
//                        } while (readIf(","));
//                        if (v.size() == 1 && (last instanceof Subquery)) {
//                            Subquery s = (Subquery) last;
//                            Query q = s.getQuery();
//                            r = new ConditionInSelect(database, r, q, false, Comparison.EQUAL);
//                        } else {
//                            r = new ConditionIn(database, r, v);
//                        }
//                    }
//                    read(")");
//                }
//            } else if (readIf("BETWEEN")) {
//                Expression low = readConcat();
//                read("AND");
//                Expression high = readConcat();
//                Expression condLow = new Comparison(session, Comparison.SMALLER_EQUAL, low, r);
//                Expression condHigh = new Comparison(session, Comparison.BIGGER_EQUAL, high, r);
//                r = new ConditionAndOr(ConditionAndOr.AND, condLow, condHigh);
            } else {
                String compareType = getCompareType(currentTokenType);
                if (compareType == null) {
                    break;
                }
                read();
//                if (readIf("ALL")) {
//                    read("(");
//                    Query query = parseSelect();
//                    r = new ConditionInSelect(database, r, query, true, compareType);
//                    read(")");
//                } else if (readIf("ANY") || readIf("SOME")) {
//                    read("(");
//                    Query query = parseSelect();
//                    r = new ConditionInSelect(database, r, query, false, compareType);
//                    read(")");
//                } else {
                    Expression right = readConcat();
//                    if (readIf("(") && readIf("+") && readIf(")")) {
//                        // support for a subset of old-fashioned Oracle outer
//                        // join with (+)
//                        if (r instanceof ExpressionColumn && right instanceof ExpressionColumn) {
//                            ExpressionColumn leftCol = (ExpressionColumn) r;
//                            ExpressionColumn rightCol = (ExpressionColumn) right;
//                            ObjectArray<TableFilter> filters = currentSelect.getTopFilters();
//                            for (TableFilter f : filters) {
//                                while (f != null) {
//                                    leftCol.mapColumns(f, 0);
//                                    rightCol.mapColumns(f, 0);
//                                    f = f.getJoin();
//                                }
//                            }
//                            TableFilter leftFilter = leftCol.getTableFilter();
//                            TableFilter rightFilter = rightCol.getTableFilter();
//                            r = new Comparison(session, compareType, r, right);
//                            if (leftFilter != null && rightFilter != null) {
//                                int idx = filters.indexOf(rightFilter);
//                                if (idx >= 0) {
//                                    filters.remove(idx);
//                                    leftFilter.addJoin(rightFilter, true, r);
//                                } else {
//                                    rightFilter.mapAndAddFilter(r);
//                                }
//                                r = ValueExpression.get(ValueBoolean.get(true));
//                            }
//                        }
//                    } else {
                        r = new Comparison(session, compareType, r, right);
//                    }
//                }

            //if (not) {
              //  r = new ConditionNot(session, r);
            //}
            }
        }
        return r;
    }

    private Expression readConcat() throws SQLException {
        Expression r = readSum();
        return r;
//        while (true) {
//            if (readIf("||")) {
//                r = new Operation(Operation.CONCAT, r, readSum());
//            } else if (readIf("~")) {
//                if (readIf("*")) {
//                    Function function = Function.getFunction(database, "CAST");
//                    function.setDataType(new Column("X", Value.STRING_IGNORECASE));
//                    function.setParameter(0, r);
//                    r = function;
//                }
//                r = new CompareLike(database.getCompareMode(), r, readSum(), null, true);
//            } else if (readIf("!~")) {
//                if (readIf("*")) {
//                    Function function = Function.getFunction(database, "CAST");
//                    function.setDataType(new Column("X", Value.STRING_IGNORECASE));
//                    function.setParameter(0, r);
//                    r = function;
//                }
//                r = new ConditionNot(new CompareLike(null, r, readSum(), null, true));
//            } else {
//                return r;
//            }
//        }
    }

    private Expression readSum() throws SQLException {
        Expression r = readFactor();
//        while (true) {
//            if (readIf("+")) {
//                r = new Operation(Operation.PLUS, r, readFactor());
//            } else if (readIf("-")) {
//                r = new Operation(Operation.MINUS, r, readFactor());
//            } else {
                return r;
//            }
//        }
    }

    private Expression readFactor() throws SQLException {
        Expression r = readTerm();
//        while (true) {
//            if (readIf("*")) {
//                r = new Operation(Operation.MULTIPLY, r, readTerm());
//            } else if (readIf("/")) {
//                r = new Operation(Operation.DIVIDE, r, readTerm());
//            } else {
                return r;
//            }
//        }
    }

    private Expression readAggregate(int aggregateType) throws SQLException {
        return null;
//        if (currentSelect == null) {
//            throw getSyntaxError();
//        }
//        currentSelect.setGroupQuery();
//        Expression r;
//        if (aggregateType == Aggregate.COUNT) {
//            if (readIf("*")) {
//                r = new Aggregate(Aggregate.COUNT_ALL, null, currentSelect, false);
//            } else {
//                boolean distinct = readIf("DISTINCT");
//                Expression on = readExpression();
//                if (on instanceof Wildcard && !distinct) {
//                    // PostgreSQL compatibility: count(t.*)
//                    r = new Aggregate(Aggregate.COUNT_ALL, null, currentSelect, false);
//                } else {
//                    r = new Aggregate(Aggregate.COUNT, on, currentSelect, distinct);
//                }
//            }
//        } else if (aggregateType == Aggregate.GROUP_CONCAT) {
//            boolean distinct = readIf("DISTINCT");
//            Aggregate agg = new Aggregate(Aggregate.GROUP_CONCAT, readExpression(), currentSelect, distinct);
//            if (readIf("ORDER")) {
//                read("BY");
//                agg.setOrder(parseSimpleOrderList());
//            }
//            if (readIf("SEPARATOR")) {
//                agg.setSeparator(readExpression());
//            }
//            r = agg;
//        } else {
//            boolean distinct = readIf("DISTINCT");
//            r = new Aggregate(aggregateType, readExpression(), currentSelect, distinct);
//        }
//        read(")");
//        return r;
    }

    private ObjectArray<Object> parseSimpleOrderList() throws SQLException {
//        ObjectArray<SelectOrderBy> orderList = ObjectArray.newInstance();
//        do {
//            SelectOrderBy order = new SelectOrderBy();
//            Expression expr = readExpression();
//            order.expression = expr;
//            if (readIf("DESC")) {
//                order.descending = true;
//            } else {
//                readIf("ASC");
//            }
//            orderList.add(order);
//        } while (readIf(","));
//        return orderList;
        return null;
    }

    private Expression readFunction(String name) throws SQLException {
        return null;
//        int agg = Aggregate.getAggregateType(name);
//        if (agg >= 0) {
//            return readAggregate(agg);
//        }
//        Function function = Function.getFunction(database, name);
//        if (function == null) {
//            UserAggregate aggregate = database.findAggregate(name);
//            if (aggregate != null) {
//                return readJavaAggregate(aggregate);
//            }
//            return readJavaFunction(name);
//        }
//        switch (function.getFunctionType()) {
//        case Function.CAST: {
//            function.setParameter(0, readExpression());
//            read("AS");
//            Column type = parseColumn(null);
//            function.setDataType(type);
//            read(")");
//            break;
//        }
//        case Function.CONVERT: {
//            function.setParameter(0, readExpression());
//            read(",");
//            Column type = parseColumn(null);
//            function.setDataType(type);
//            read(")");
//            break;
//        }
//        case Function.EXTRACT: {
//            function.setParameter(0, ValueExpression.get(ValueString.get(currentToken)));
//            read();
//            read("FROM");
//            function.setParameter(1, readExpression());
//            read(")");
//            break;
//        }
//        case Function.DATE_DIFF: {
//            if (Function.isDatePart(currentToken)) {
//                function.setParameter(0, ValueExpression.get(ValueString.get(currentToken)));
//                read();
//            } else {
//                function.setParameter(0, readExpression());
//            }
//            read(",");
//            function.setParameter(1, readExpression());
//            read(",");
//            function.setParameter(2, readExpression());
//            read(")");
//            break;
//        }
//        case Function.SUBSTRING: {
//            function.setParameter(0, readExpression());
//            if (!readIf(",")) {
//                read("FROM");
//            }
//            function.setParameter(1, readExpression());
//            if (readIf("FOR") || readIf(",")) {
//                function.setParameter(2, readExpression());
//            }
//            read(")");
//            break;
//        }
//        case Function.POSITION: {
//            // can't read expression because IN would be read too early
//            function.setParameter(0, readConcat());
//            if (!readIf(",")) {
//                read("IN");
//            }
//            function.setParameter(1, readExpression());
//            read(")");
//            break;
//        }
//        case Function.TRIM: {
//            Expression space = null;
//            if (readIf("LEADING")) {
//                function = Function.getFunction(database, "LTRIM");
//                if (!readIf("FROM")) {
//                    space = readExpression();
//                    read("FROM");
//                }
//            } else if (readIf("TRAILING")) {
//                function = Function.getFunction(database, "RTRIM");
//                if (!readIf("FROM")) {
//                    space = readExpression();
//                    read("FROM");
//                }
//            } else if (readIf("BOTH")) {
//                if (!readIf("FROM")) {
//                    space = readExpression();
//                    read("FROM");
//                }
//            }
//            Expression p0 = readExpression();
//            if (readIf(",")) {
//                space = readExpression();
//            } else if (readIf("FROM")) {
//                space = p0;
//                p0 = readExpression();
//            }
//            function.setParameter(0, p0);
//            if (space != null) {
//                function.setParameter(1, space);
//            }
//            read(")");
//            break;
//        }
//        case Function.TABLE:
//        case Function.TABLE_DISTINCT: {
//            int i = 0;
//            ObjectArray<Column> columns = ObjectArray.newInstance();
//            do {
//                String columnName = readAliasIdentifier();
//                Column column = parseColumn(columnName);
//                columns.add(column);
//                read("=");
//                function.setParameter(i, readExpression());
//                i++;
//            } while (readIf(","));
//            read(")");
//            TableFunction tf = (TableFunction) function;
//            tf.setColumns(columns);
//            break;
//        }
//        default:
//            if (!readIf(")")) {
//                int i = 0;
//                do {
//                    function.setParameter(i++, readExpression());
//                } while (readIf(","));
//                read(")");
//            }
//        }
//        function.doneWithParameters();
//        return function;
    }

    private Function readFunctionWithoutParameters(String name) throws SQLException {
//        if (readIf("(")) {
//            read(")");
//        }
//        Function function = Function.getFunction(database, name);
//        function.doneWithParameters();
//        return function;
        return null;
    }

    private Expression readWildcardOrSequenceValue(String schema, String objectName) throws SQLException {
        if (readIf("*")) {
            return new Wildcard(session, schema, objectName);
        }
//        if (schema == null) {
//            schema = session.getCurrentSchemaName();
//        }
//        if (readIf("NEXTVAL")) {
//            Sequence sequence = findSequence(schema, objectName);
//            if (sequence != null) {
//                return new SequenceValue(sequence);
//            }
//        } else if (readIf("CURRVAL")) {
//            Sequence sequence = findSequence(schema, objectName);
//            if (sequence != null) {
//                Function function = Function.getFunction(database, "CURRVAL");
//                function.setParameter(0, ValueExpression.get(ValueString.get(sequence.getSchema().getName())));
//                function.setParameter(1, ValueExpression.get(ValueString.get(sequence.getName())));
//                function.doneWithParameters();
//                return function;
//            }
//        }
        return null;
    }

    private Expression readTermObjectDot(String objectName) throws SQLException {
        Expression expr = readWildcardOrSequenceValue(null, objectName);
        if (expr != null) {
            return expr;
        }
        String name = readColumnIdentifier();
//        if (readIf(".")) {
//            String schema = objectName;
//            objectName = name;
//            expr = readWildcardOrSequenceValue(schema, objectName);
//            if (expr != null) {
//                return expr;
//            }
//            name = readColumnIdentifier();
//            if (readIf(".")) {
//                String databaseName = schema;
//                if (!database.getShortName().equals(databaseName)) {
//                    throw Message.getSQLException(ErrorCode.DATABASE_NOT_FOUND_1, databaseName);
//                }
//                schema = objectName;
//                objectName = name;
//                expr = readWildcardOrSequenceValue(schema, objectName);
//                if (expr != null) {
//                    return expr;
//                }
//                name = readColumnIdentifier();
//                return new ExpressionColumn(database, schema, objectName, name);
//            }
//            return new ExpressionColumn(database, schema, objectName, name);
//        }
        return new ExpressionColumn(session, null, objectName, name);
    }

    private Expression readTerm() throws SQLException {
        Expression r = null;
        switch (currentTokenType) {
//        case AT:
//            read();
//            r = new Variable(session, readAliasIdentifier());
//            if (readIf(":=")) {
//                Expression value = readExpression();
//                Function function = Function.getFunction(database, "SET");
//                function.setParameter(0, r);
//                function.setParameter(1, value);
//                r = function;
//            }
//            break;
        case PARAMETER:
            // there must be no space between ? and the number
            boolean indexed = Character.isDigit(sqlCommandChars[parseIndex]);
            read();
            Parameter p;
//            if (indexed && currentTokenType == VALUE && currentValue.getType() == Value.INT) {
//                if (indexedParameterList == null) {
//                    if (parameters == null) {
//                        // this can occur when parsing expressions only (for example check constraints)
//                        throw getSyntaxError();
//                    } else if (parameters.size() > 0) {
//                        throw Message.getSQLException(ErrorCode.CANNOT_MIX_INDEXED_AND_UNINDEXED_PARAMS);
//                    }
//                    indexedParameterList = ObjectArray.newInstance();
//                }
//                int index = 0;//currentValue.getInt() - 1;
//                if (index < 0 || index >= Constants.MAX_PARAMETER_INDEX) {
//                    throw Message.getInvalidValueException("" + index, "Parameter Index");
//                }
//                if (indexedParameterList.size() <= index) {
//                    indexedParameterList.setSize(index + 1);
//                }
//                p = indexedParameterList.get(index);
//                if (p == null) {
//                    p = new Parameter(index);
//                    indexedParameterList.set(index, p);
//                }
//                read();
//            } else {
//                if (indexedParameterList != null) {
//                    throw Message.getSQLException(ErrorCode.CANNOT_MIX_INDEXED_AND_UNINDEXED_PARAMS);
//                }
                p = new Parameter(session, parameters.size());
//            }
            parameters.add(p);
            r = p;
            break;
//        case KEYWORD:
//            if (isToken("SELECT") || isToken("FROM")) {
//                Query query = parseSelect();
//                r = new Subquery(query);
//            } else {
//                throw getSyntaxError();
//            }
//            break;
        case IDENTIFIER:
            String name = currentToken;
//            if (currentTokenQuoted) {
//                read();
//                if (readIf("(")) {
//                    r = readFunction(name);
//                } else if (readIf(".")) {
//                    r = readTermObjectDot(name);
//                } else {
//                    r = new ExpressionColumn(database, null, null, name);
//                }
//            } else {
                read();
//                if ("X".equals(name) && currentTokenType == VALUE && currentValue.getType() == Value.STRING) {
//                    read();
//                 //   byte[] buffer = ByteUtils.convertStringToBytes(currentValue.getString());
//                    r = null;//ValueExpression.get(ValueBytes.getNoCopy(buffer));
//                } else
                  if (readIf(".")) {
                    r = readTermObjectDot(name);
//                } else if ("CASE".equals(name)) {
//                    // CASE must be processed before (,
//                    // otherwise CASE(3) would be a function call, which it is
//                    // not
//                    if (isToken("WHEN")) {
//                        r = readWhen(null);
//                    } else {
//                        Expression left = readExpression();
//                        r = readWhen(left);
//                    }
//                } else if (readIf("(")) {
//                    r = readFunction(name);
//                } else if ("CURRENT_USER".equals(name)) {
//                    r = readFunctionWithoutParameters("USER");
//                } else if ("CURRENT".equals(name)) {
//                    if (readIf("TIMESTAMP")) {
//                        r = readFunctionWithoutParameters("CURRENT_TIMESTAMP");
//                    } else if (readIf("TIME")) {
//                        r = readFunctionWithoutParameters("CURRENT_TIME");
//                    } else if (readIf("DATE")) {
//                        r = readFunctionWithoutParameters("CURRENT_DATE");
//                    } else {
//                        r = new ExpressionColumn(database, null, null, name);
//                    }
//                } else if ("NEXT".equals(name) && readIf("VALUE")) {
//                    read("FOR");
//                    Sequence sequence = readSequence();
//                    r = new SequenceValue(sequence);
//                } else if ("DATE".equals(name) && currentTokenType == VALUE && currentValue.getType() == Value.STRING) {
//                    String date = currentValue.getString();
//                    read();
//                    //r = ValueExpression.get(ValueDate.get(ValueDate.parseDate(date)));
//                } else if ("TIME".equals(name) && currentTokenType == VALUE && currentValue.getType() == Value.STRING) {
//                    String time = currentValue.getString();
//                    read();
//                   // r = ValueExpression.get(ValueTime.get(ValueTime.parseTime(time)));
//                } else if ("TIMESTAMP".equals(name) && currentTokenType == VALUE
//                        && currentValue.getType() == Value.STRING) {
//                    String timestamp = currentValue.getString();
//                    read();
//                    //r = ValueExpression.get(ValueTimestamp.getNoCopy(ValueTimestamp.parseTimestamp(timestamp)));
//                } else if ("E".equals(name) && currentTokenType == VALUE && currentValue.getType() == Value.STRING) {
//                    String text = currentValue.getString();
//                    read();
//                    r = ValueExpression.get(ValueString.get(text));
                } else {
                    r = new ExpressionColumn(session, null, null, name);
                }
//            }
            break;
//        case MINUS:
//            read();
//            if (currentTokenType == VALUE) {
//                //r = ValueExpression.get(currentValue.negate());
//                // convert Integer.MIN_VALUE to int (-Integer.MIN_VALUE needed
//                // to be a long)
//              //  if (r.getType() == Value.LONG && r.getValue(session).getLong() == Integer.MIN_VALUE) {
//               //     r = ValueExpression.get(ValueInt.get(Integer.MIN_VALUE));
//                //}
//                read();
//            } else {
//                r = new Operation(Operation.NEGATE, readTerm(), null);
//            }
//            break;
//        case PLUS:
//            read();
//            r = readTerm();
//            break;
        case OPEN:
            read();
            r = readExpression();
            if (readIf(",")) {
                ObjectArray<Expression> list = ObjectArray.newInstance();
                list.add(r);
                do {
                    r = readExpression();
                    list.add(r);
                } while (readIf(","));
                Expression[] array = new Expression[list.size()];
                list.toArray(array);
                // XXX r = new ExpressionList(array);
            }
            read(")");
            break;
//        case TRUE:
//            read();
//            r = ValueExpression.get(ValueBoolean.get(true));
//            break;
//        case FALSE:
//            read();
//            r = ValueExpression.get(ValueBoolean.get(false));
//            break;
//        case CURRENT_TIME:
//            read();
//            r = readFunctionWithoutParameters("CURRENT_TIME");
//            break;
//        case CURRENT_DATE:
//            read();
//            r = readFunctionWithoutParameters("CURRENT_DATE");
//            break;
//        case CURRENT_TIMESTAMP: {
//            Function function = Function.getFunction(database, "CURRENT_TIMESTAMP");
//            read();
//            if (readIf("(")) {
//                if (!readIf(")")) {
//                    function.setParameter(0, readExpression());
//                    read(")");
//                }
//            }
//            function.doneWithParameters();
//            r = function;
//            break;
//        }
//        case ROWNUM:
//            read();
//            if (readIf("(")) {
//                read(")");
//            }
//            r = new Rownum(currentSelect == null ? currentPrepared : currentSelect);
//            break;
        case NULL:
            read();
            r = new Null();
            break;
//        case VALUE:
//            r = ValueExpression.get(currentValue);
//            read();
//            break;
        default:
            throw getSyntaxError();
        }
//        if (readIf("[")) {
//            Function function = Function.getFunction(database, "ARRAY_GET");
//            function.setParameter(0, r);
//            r = readExpression();
//            r = new Operation(Operation.PLUS, r, null);//ValueExpression.get(ValueInt.get(1)));
//            function.setParameter(1, r);
//            r = function;
//            read("]");
//        }
//        if (readIf("::")) {
//            // PostgreSQL compatibility
//            Column col = parseColumn(null);
//            Function function = Function.getFunction(database, "CAST");
//            function.setDataType(col);
//            function.setParameter(0, r);
//            r = function;

        return r;
    }

    private Expression readWhen(Expression left) throws SQLException {
//        if (readIf("END")) {
//            readIf("CASE");
//            return ValueExpression.getNull();
//        }
//        if (readIf("ELSE")) {
//            Expression elsePart = readExpression();
//            read("END");
//            readIf("CASE");
//            return elsePart;
//        }
//        readIf("WHEN");
//        Expression when = readExpression();
//        if (left != null) {
//            when = new Comparison(session, Comparison.EQUAL, left, when);
//        }
//        read("THEN");
//        Expression then = readExpression();
//        Expression elsePart = readWhen(left);
//        Function function = Function.getFunction(session.getDatabase(), "CASEWHEN");
//        function.setParameter(0, when);
//        function.setParameter(1, then);
//        function.setParameter(2, elsePart);
//        function.doneWithParameters();
//        return function;
        return null;
    }

    private int getPositiveInt() throws SQLException {
        return 1;
//        int v = getInt();
//        if (v < 0) {
//            throw Message.getInvalidValueException("" + v, "positive integer");
//        }
//        return v;
    }

    private int getInt() throws SQLException {
        return 1;
//        boolean minus = false;
//        if (currentTokenType == MINUS) {
//            minus = true;
//            read();
//        } else if (currentTokenType == PLUS) {
//            read();
//        }
//        if (currentTokenType != VALUE || currentValue.getType() != Value.INT) {
//            throw Message.getSyntaxError(sqlCommand, parseIndex, "integer");
//        }
//        int i = 1;//currentValue.getInt();
//        read();
//        return minus ? -i : i;
    }

    private long readLong() throws SQLException {
        return 1;
//        boolean minus = false;
//        if (currentTokenType == MINUS) {
//            minus = true;
//            read();
//        }
//        if (currentTokenType != VALUE
//                || (currentValue.getType() != Value.INT && currentValue.getType() != Value.DECIMAL)) {
//            throw Message.getSyntaxError(sqlCommand, parseIndex, "long");
//        }
//        long i = 1;//currentValue.getLong();
//        read();
//        return minus ? -i : i;
    }

    private String readString() throws SQLException {
//        Expression expr = readExpression();
//        if (!(expr instanceof ValueExpression)) {
//            throw Message.getSyntaxError(sqlCommand, parseIndex, "string");
//        }
//        String s = expr.getValue(session).getString();
//        return s;
        return null;
    }

    private String readIdentifierWithSchema(String defaultSchemaName) throws SQLException {
        if (currentTokenType != IDENTIFIER) {
            throw Message.getSyntaxError(sqlCommand, parseIndex, "identifier");
        }
        String s = currentToken;
        read();
//        schemaName = defaultSchemaName;
//        if (readIf(".")) {
//            schemaName = s;
//            if (currentTokenType != IDENTIFIER) {
//                throw Message.getSyntaxError(sqlCommand, parseIndex, "identifier");
//            }
//            s = currentToken;
//            read();
//        }
//        if (".".equals(currentToken)) {
//            if (schemaName.equalsIgnoreCase(database.getShortName())) {
//                read(".");
//                schemaName = s;
//                if (currentTokenType != IDENTIFIER) {
//                    throw Message.getSyntaxError(sqlCommand, parseIndex, "identifier");
//                }
//                s = currentToken;
//                read();
//            }
//        }
        return s;
    }

    private String readIdentifierWithSchema() throws SQLException {
        return null; //readIdentifierWithSchema(session.getCurrentSchemaName());
    }

    private String readAliasIdentifier() throws SQLException {
        return readColumnIdentifier();
    }

    private String readUniqueIdentifier() throws SQLException {
        return readColumnIdentifier();
    }

    private String readColumnIdentifier() throws SQLException {
        if (currentTokenType != IDENTIFIER) {
            throw Message.getSyntaxError(sqlCommand, parseIndex, "identifier");
        }
        String s = currentToken;
        read();
        return s;
    }

    private void read(String expected) throws SQLException {
        if (!expected.equals(currentToken) || currentTokenQuoted) {
            throw Message.getSyntaxError(sqlCommand, parseIndex, expected);
        }
        read();
    }

    private Operator readCustom(boolean not) throws SQLException {
        String token = not ? "NOT " + currentToken : currentToken;
        Operator op = session.getDatabase().getOperatorByName(token);
        if (op != null) {
            read();
            return op;
        }
        return null;
    }

    private boolean readIf(String token) throws SQLException {
        if (token.equals(currentToken) && !currentTokenQuoted) {
            read();
            return true;
        }
        addExpected(token);
        return false;
    }

    private boolean isToken(String token) {
        boolean result = token.equals(currentToken) && !currentTokenQuoted;
        if (result) {
            return true;
        }
        addExpected(token);
        return false;
    }

    private void addExpected(String token) {
        if (expectedList != null) {
            expectedList.add(token);
        }
    }

    private void read() throws SQLException {
        currentTokenQuoted = false;
        if (expectedList != null) {
            expectedList.clear();
        }
        int[] types = characterTypes;
        lastParseIndex = parseIndex;
        int i = parseIndex;
        int type = types[i];
        while (type == 0) {
            type = types[++i];
        }
        int start = i;
        char[] chars = sqlCommandChars;
        char c = chars[i++];
        currentToken = "";
        switch (type) {
        case CHAR_NAME:
            while (true) {
                type = types[i];
                if (type != CHAR_NAME && type != CHAR_VALUE) {
                    break;
                }
                i++;
            }
            currentToken = sqlCommand.substring(start, i);
            currentTokenType = getTokenType(currentToken);
            parseIndex = i;
            return;
        case CHAR_QUOTED: {
            String result = null;
            while (true) {
                for (int begin = i;; i++) {
                    if (chars[i] == '\"') {
                        if (result == null) {
                            result = sqlCommand.substring(begin, i);
                        } else {
                            result += sqlCommand.substring(begin - 1, i);
                        }
                        break;
                    }
                }
                if (chars[++i] != '\"') {
                    break;
                }
                i++;
            }
            currentToken = result;
            parseIndex = i;
            currentTokenQuoted = true;
            currentTokenType = IDENTIFIER;
            return;
        }
        case CHAR_SPECIAL_2:
            if (types[i] == CHAR_SPECIAL_2) {
                i++;
            }
            currentToken = sqlCommand.substring(start, i);
            currentTokenType = getSpecialType(currentToken);
            parseIndex = i;
            return;
        case CHAR_SPECIAL_1:
            currentToken = sqlCommand.substring(start, i);
            currentTokenType = getSpecialType(currentToken);
            parseIndex = i;
            return;
        case CHAR_VALUE:
            if (c == '0' && chars[i] == 'X') {
                // hex number
                long number = 0;
                start += 2;
                i++;
                while (true) {
                    c = chars[i];
                    if ((c < '0' || c > '9') && (c < 'A' || c > 'F')) {
                        checkLiterals(false);
                        currentValue = null; //ValueInt.get((int) number);
                        currentTokenType = VALUE;
                        currentToken = "0";
                        parseIndex = i;
                        return;
                    }
                    number = (number << 4) + c - (c >= 'A' ? ('A' - 0xa) : ('0'));
                    if (number > Integer.MAX_VALUE) {
                        readHexDecimal(start, i);
                        return;
                    }
                    i++;
                }
            }
            long number = c - '0';
            while (true) {
                c = chars[i];
                if (c < '0' || c > '9') {
                    if (c == '.') {
                        readDecimal(start, i);
                        break;
                    }
                    if (c == 'E') {
                        readDecimal(start, i);
                        break;
                    }
                    checkLiterals(false);
                    currentValue = null; //ValueInt.get((int) number);
                    currentTokenType = VALUE;
                    currentToken = "0";
                    parseIndex = i;
                    break;
                }
                number = number * 10 + (c - '0');
                if (number > Integer.MAX_VALUE) {
                    readDecimal(start, i);
                    break;
                }
                i++;
            }
            return;
        case CHAR_DECIMAL:
            if (types[i] != CHAR_VALUE) {
                currentTokenType = KEYWORD;
                currentToken = ".";
                parseIndex = i;
                return;
            }
            readDecimal(i - 1, i);
            return;
        case CHAR_STRING: {
            String result = null;
            while (true) {
                for (int begin = i;; i++) {
                    if (chars[i] == '\'') {
                        if (result == null) {
                            result = sqlCommand.substring(begin, i);
                        } else {
                            result += sqlCommand.substring(begin - 1, i);
                        }
                        break;
                    }
                }
                if (chars[++i] != '\'') {
                    break;
                }
                i++;
            }
            currentToken = "'";
            checkLiterals(true);
            currentValue = null; //ValueString.get(StringCache.getNew(result));
            parseIndex = i;
            currentTokenType = VALUE;
            return;
        }
        case CHAR_DOLLAR_QUOTED_STRING: {
            String result = null;
            int begin = i - 1;
            while (types[i] == CHAR_DOLLAR_QUOTED_STRING) {
                i++;
            }
            result = sqlCommand.substring(begin, i);
            currentToken = "'";
            checkLiterals(true);
            currentValue = null; //ValueString.get(StringCache.getNew(result));
            parseIndex = i;
            currentTokenType = VALUE;
            return;
        }
        case CHAR_END:
            currentToken = "";
            currentTokenType = END;
            parseIndex = i;
            return;
        default:
            throw getSyntaxError();
        }
    }

    private void checkLiterals(boolean text) throws SQLException {
//        if (!session.getAllowLiterals()) {
//            int allowed = database.getAllowLiterals();
//            if (allowed == Constants.ALLOW_LITERALS_NONE || (text && allowed != Constants.ALLOW_LITERALS_ALL)) {
//                throw Message.getSQLException(ErrorCode.LITERALS_ARE_NOT_ALLOWED);
//            }
//        }
    }

    private void readHexDecimal(int start, int i) throws SQLException {
        char[] chars = sqlCommandChars;
        char c;
        do {
            c = chars[++i];
        } while ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F'));
        parseIndex = i;
        String sub = sqlCommand.substring(start, i);
        BigDecimal bd = new BigDecimal(new BigInteger(sub, 16));
        checkLiterals(false);
     //   currentValue = null;//ValueDecimal.get(bd);
        currentTokenType = VALUE;
    }

    private void readDecimal(int start, int i) throws SQLException {
        char[] chars = sqlCommandChars;
        int[] types = characterTypes;
        // go until the first non-number
        while (true) {
            int t = types[i];
            if (t != CHAR_DECIMAL && t != CHAR_VALUE) {
                break;
            }
            i++;
        }
        if (chars[i] == 'E') {
            i++;
            if (chars[i] == '+' || chars[i] == '-') {
                i++;
            }
            if (types[i] != CHAR_VALUE) {
                throw getSyntaxError();
            }
            while (types[++i] == CHAR_VALUE) {
                // go until the first non-number
            }
        }
        parseIndex = i;
        String sub = sqlCommand.substring(start, i);
        BigDecimal bd;
        try {
            bd = new BigDecimal(sub);
        } catch (NumberFormatException e) {
            throw Message.getSQLException(ErrorCode.DATA_CONVERSION_ERROR_1, e, sub);
        }
        checkLiterals(false);
        currentValue = null; //ValueDecimal.get(bd);
        currentTokenType = VALUE;
    }

    public Session getSession() {
        return null;
    }

    private void initialize(String sql) throws SQLException {
        if (sql == null) {
            sql = "";
        }
        originalSQL = sql;
        sqlCommand = sql;
        int len = sql.length() + 1;
        char[] command = new char[len];
        int[] types = new int[len];
        len--;
        sql.getChars(0, len, command, 0);
        boolean changed = false;
        command[len] = ' ';
        int startLoop = 0;
        int lastType = 0;
        for (int i = 0; i < len; i++) {
            char c = command[i];
            int type = 0;
            switch (c) {
            case '/':
                if (command[i + 1] == '*') {
                    // block comment
                    changed = true;
                    command[i] = ' ';
                    command[i + 1] = ' ';
                    startLoop = i;
                    i += 2;
                    checkRunOver(i, len, startLoop);
                    while (command[i] != '*' || command[i + 1] != '/') {
                        command[i++] = ' ';
                        checkRunOver(i, len, startLoop);
                    }
                    command[i] = ' ';
                    command[i + 1] = ' ';
                    i++;
                } else if (command[i + 1] == '/') {
                    // single line comment
                    changed = true;
                    startLoop = i;
                    while (true) {
                        c = command[i];
                        if (c == '\n' || c == '\r' || i >= len - 1) {
                            break;
                        }
                        command[i++] = ' ';
                        checkRunOver(i, len, startLoop);
                    }
                } else {
                    type = CHAR_SPECIAL_1;
                }
                break;
            case '-':
                if (command[i + 1] == '-') {
                    // single line comment
                    changed = true;
                    startLoop = i;
                    while (true) {
                        c = command[i];
                        if (c == '\n' || c == '\r' || i >= len - 1) {
                            break;
                        }
                        command[i++] = ' ';
                        checkRunOver(i, len, startLoop);
                    }
                } else {
                    type = CHAR_SPECIAL_1;
                }
                break;
            case '$':
                if (false && command[i + 1] == '$' && (i == 0 || command[i - 1] <= ' ')) {
                    // dollar quoted string
                    changed = true;
                    command[i] = ' ';
                    command[i + 1] = ' ';
                    startLoop = i;
                    i += 2;
                    checkRunOver(i, len, startLoop);
                    while (command[i] != '$' || command[i + 1] != '$') {
                        types[i++] = CHAR_DOLLAR_QUOTED_STRING;
                        checkRunOver(i, len, startLoop);
                    }
                    command[i] = ' ';
                    command[i + 1] = ' ';
                    i++;
                } else {
                    if (lastType == CHAR_NAME || lastType == CHAR_VALUE) {
                        // $ inside an identifier is supported
                        type = CHAR_NAME;
                    } else {
                        // but not at the start, to support PostgreSQL $1
                        type = CHAR_SPECIAL_1;
                    }
                }
                break;
            case '(':
            case ')':
            case '{':
            case '}':
            case '*':
            case ',':
            case ';':
            case '+':
            case '%':
            case '?':
            case '@':
            case ']':
                type = CHAR_SPECIAL_1;
                break;
            case '!':
            case '<':
            case '>':
            case '|':
            case '=':
            case ':':
            case '~':
                type = CHAR_SPECIAL_2;
                break;
            case '.':
                type = CHAR_DECIMAL;
                break;
            case '\'':
                type = types[i] = CHAR_STRING;
                startLoop = i;
                while (command[++i] != '\'') {
                    checkRunOver(i, len, startLoop);
                }
                break;
            case '[':
                if (false) {
                    // SQL Server alias for "
                    command[i] = '"';
                    changed = true;
                    type = types[i] = CHAR_QUOTED;
                    startLoop = i;
                    while (command[++i] != ']') {
                        checkRunOver(i, len, startLoop);
                    }
                    command[i] = '"';
                } else {
                    type = CHAR_SPECIAL_1;
                }
                break;
            case '`':
                // MySQL alias for ", but not case sensitive
                command[i] = '"';
                changed = true;
                type = types[i] = CHAR_QUOTED;
                startLoop = i;
                while (command[++i] != '`') {
                    checkRunOver(i, len, startLoop);
                    c = command[i];
                    command[i] = Character.toUpperCase(c);
                }
                command[i] = '"';
                break;
            case '\"':
                type = types[i] = CHAR_QUOTED;
                startLoop = i;
                while (command[++i] != '\"') {
                    checkRunOver(i, len, startLoop);
                }
                break;
            case '_':
                type = CHAR_NAME;
                break;
            default:
                if (c >= 'a' && c <= 'z') {
                    command[i] = (char) (c - ('a' - 'A'));
                    changed = true;
                    type = CHAR_NAME;
                } else if (c >= 'A' && c <= 'Z') {
                    type = CHAR_NAME;
                } else if (c >= '0' && c <= '9') {
                    type = CHAR_VALUE;
                } else {
                    if (false) { // TODO 95 Character.isJavaIdentifierPart(c)) {
                        type = CHAR_NAME;
                        char u = Character.toUpperCase(c);
                        if (u != c) {
                            command[i] = u;
                            changed = true;
                        }
                    }
                }
            }
            types[i] = type;
            lastType = type;
        }
        sqlCommandChars = command;
        types[len] = CHAR_END;
        characterTypes = types;
        if (changed) {
            sqlCommand = new String(command);
        }
        parseIndex = 0;
    }

    private void checkRunOver(int i, int len, int startLoop) throws SQLException {
        if (i >= len) {
            parseIndex = startLoop;
            throw getSyntaxError();
        }
    }

    private int getSpecialType(String s) throws SQLException {
        char c0 = s.charAt(0);
        if (s.length() == 1) {
            switch (c0) {
            case '?':
            case '$':
                return PARAMETER;
            case '@':
                return AT;
            case '+':
                return PLUS;
            case '-':
                return MINUS;
            case '{':
            case '}':
            case '*':
            case '/':
            case ';':
            case ',':
            case ':':
            case '[':
            case ']':
            case '~':
                return KEYWORD;
            case '(':
                return OPEN;
            case ')':
                return CLOSE;
            case '<':
                return SMALLER;
            case '>':
                return BIGGER;
            case '=':
                return EQUAL;
            default:
                break;
            }
        } else if (s.length() == 2) {
            switch (c0) {
            case ':':
                if ("::".equals(s)) {
                    return KEYWORD;
                } else if (":=".equals(s)) {
                    return KEYWORD;
                }
                break;
            case '>':
                if (">=".equals(s)) {
                    return BIGGER_EQUAL;
                }
                break;
            case '<':
                if ("<=".equals(s)) {
                    return SMALLER_EQUAL;
                } else if ("<>".equals(s)) {
                    return NOT_EQUAL;
                }
                break;
            case '!':
                if ("!=".equals(s)) {
                    return NOT_EQUAL;
                } else if ("!~".equals(s)) {
                    return KEYWORD;
                }
                break;
            case '|':
                if ("||".equals(s)) {
                    return STRING_CONCAT;
                }
                break;
            }
        }
        throw getSyntaxError();
    }

    private int getTokenType(String s) throws SQLException {
        int len = s.length();
        if (len == 0) {
            throw getSyntaxError();
        }
        return getSaveTokenType(s, false); //database.getMode().supportOffsetFetch);
    }

    /**
     * Checks if this string is a SQL keyword.
     *
     * @param s the token to check
     * @param supportOffsetFetch if OFFSET and FETCH are keywords
     * @return true if it is a keyword
     */
    public static boolean isKeyword(String s, boolean supportOffsetFetch) {
        if (s == null || s.length() == 0) {
            return false;
        }
        return getSaveTokenType(s, supportOffsetFetch) != IDENTIFIER;
    }

    private static int getSaveTokenType(String s, boolean supportOffsetFetch) {
        switch (s.charAt(0)) {
        case 'C':
            if (s.equals("CURRENT_TIMESTAMP")) {
                return CURRENT_TIMESTAMP;
            } else if (s.equals("CURRENT_TIME")) {
                return CURRENT_TIME;
            } else if (s.equals("CURRENT_DATE")) {
                return CURRENT_DATE;
            }
            return getKeywordOrIdentifier(s, "CROSS", KEYWORD);
        case 'D':
            return getKeywordOrIdentifier(s, "DISTINCT", KEYWORD);
        case 'E':
            if ("EXCEPT".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "EXISTS", KEYWORD);
        case 'F':
            if ("FROM".equals(s)) {
                return KEYWORD;
            } else if ("FOR".equals(s)) {
                return KEYWORD;
            } else if ("FULL".equals(s)) {
                return KEYWORD;
            } else if (supportOffsetFetch && "FETCH".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "FALSE", FALSE);
        case 'G':
            return getKeywordOrIdentifier(s, "GROUP", KEYWORD);
        case 'H':
            return getKeywordOrIdentifier(s, "HAVING", KEYWORD);
        case 'I':
            if ("INNER".equals(s)) {
                return KEYWORD;
            } else if ("INTERSECT".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "IS", KEYWORD);
        case 'J':
            return getKeywordOrIdentifier(s, "JOIN", KEYWORD);
        case 'L':
            if ("LIMIT".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "LIKE", KEYWORD);
        case 'M':
            return getKeywordOrIdentifier(s, "MINUS", KEYWORD);
        case 'N':
            if ("NOT".equals(s)) {
                return KEYWORD;
            } else if ("NATURAL".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "NULL", NULL);
        case 'O':
            if ("ON".equals(s)) {
                return KEYWORD;
            } else if (supportOffsetFetch && "OFFSET".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "ORDER", KEYWORD);
        case 'P':
            return getKeywordOrIdentifier(s, "PRIMARY", KEYWORD);
        case 'R':
            return getKeywordOrIdentifier(s, "ROWNUM", ROWNUM);
        case 'S':
            if (s.equals("SYSTIMESTAMP")) {
                return CURRENT_TIMESTAMP;
            } else if (s.equals("SYSTIME")) {
                return CURRENT_TIME;
            } else if (s.equals("SYSDATE")) {
                return CURRENT_TIMESTAMP;
            }
            return getKeywordOrIdentifier(s, "SELECT", KEYWORD);
        case 'T':
            if ("TODAY".equals(s)) {
                return CURRENT_DATE;
            }
            return getKeywordOrIdentifier(s, "TRUE", TRUE);
        case 'U':
            if ("UNIQUE".equals(s)) {
                return KEYWORD;
            }
            return getKeywordOrIdentifier(s, "UNION", KEYWORD);
        case 'W':
            return getKeywordOrIdentifier(s, "WHERE", KEYWORD);
        default:
            return IDENTIFIER;
        }
    }

    private static int getKeywordOrIdentifier(String s1, String s2, int keywordType) {
        if (s1.equals(s2)) {
            return keywordType;
        }
        return IDENTIFIER;
    }

    private Column parseColumnForTable(String columnName) throws SQLException {
       return null;
    }

    private void parseAutoIncrement(Column column) throws SQLException {
        long start = 1, increment = 1;
        if (readIf("(")) {
            start = readLong();
            if (readIf(",")) {
                increment = readLong();
            }
            read(")");
        }
      //  column.setAutoIncrement(true, start, increment);
    }

    private String readCommentIf() throws SQLException {
        if (readIf("COMMENT")) {
            readIf("IS");
            return readString();
        }
        return null;
    }

    private Column parseColumn(String columnName) throws SQLException {
        return null;
    }

    private Prepared parseCreate() throws SQLException {
       return null;
    }

    private boolean addRoleOrRight(int command) throws SQLException {
       return false;
    }

    private Prepared parseGrantRevoke(int operationType) throws SQLException {
        return null;
    }

    private Prepared parserCall() throws SQLException {
//        Call command = new Call(session);
//        currentPrepared = command;
//        command.setValue(readExpression());
//        return command;
        return null;
    }

    private Prepared parseCreateRole() throws SQLException {
        return null;
    }

    private Prepared parseCreateSchema() throws SQLException {
        return null;
    }

    private Prepared parseCreateSequence() throws SQLException {
        return null;
    }

    private boolean readIfNoExists() throws SQLException {
        if (readIf("IF")) {
            read("NOT");
            read("EXISTS");
            return true;
        }
        return false;
    }

    private Prepared parseCreateConstant() throws SQLException {
        return null;
    }

    private Prepared parseCreateAggregate(boolean force) throws SQLException {
//        boolean ifNotExists = readIfNoExists();
//        CreateAggregate command = new CreateAggregate(session);
//        command.setForce(force);
//        String name = readUniqueIdentifier();
//        if (isKeyword(name, false) || Function.getFunction(database, name) != null || Aggregate.getAggregateType(name) >= 0) {
//            throw Message.getSQLException(ErrorCode.FUNCTION_ALIAS_ALREADY_EXISTS_1, name);
//        }
//        command.setName(name);
//        command.setIfNotExists(ifNotExists);
//        read("FOR");
//        command.setJavaClassMethod(readUniqueIdentifier());
//        return command;
        return null;
    }

    private Prepared parseCreateUserDataType() throws SQLException {
        return null;
    }

    private Prepared parseCreateTrigger(boolean force) throws SQLException {
       return null;
    }

    private Prepared parseCreateUser() throws SQLException {
        return null;
    }

    private Prepared parseCreateFunctionAlias(boolean force) throws SQLException {
      return null;
    }

    private Prepared parserWith() throws SQLException {
        return null;
    }

    private Prepared parseCreateView(boolean force) throws SQLException {
        return null;
    }

    private Prepared parseCheckpoint() throws SQLException {
        return null;
    }

    private Prepared parseAlter() throws SQLException {
        if (readIf("TABLE")) {
            return parseAlterTable();
        } else if (readIf("USER")) {
            return parseAlterUser();
        } else if (readIf("INDEX")) {
            return parseAlterIndex();
        } else if (readIf("SEQUENCE")) {
            return parseAlterSequence();
        } else if (readIf("VIEW")) {
            return parseAlterView();
        }
        throw getSyntaxError();
    }

    private void checkSchema(Schema old) throws SQLException {
        if (old != null && getSchema() != old) {
            throw Message.getSQLException(ErrorCode.SCHEMA_NAME_MUST_MATCH);
        }
    }

    private Prepared parseAlterIndex() throws SQLException {
        return null;
//        String indexName = readIdentifierWithSchema();
//        Schema old = getSchema();
//        AlterIndexRename command = new AlterIndexRename(session);
//
//        read("RENAME");
//        read("TO");
//        String newName = readIdentifierWithSchema(old.getName());
//        checkSchema(old);
//        command.setNewName(newName);
//        return command;
    }

    private Prepared parseAlterView() throws SQLException {
//        AlterView command = new AlterView(session);
//        String viewName = readIdentifierWithSchema();
//        Table tableView = getSchema().findTableOrView(session, viewName);
//        if (!(tableView instanceof TableView)) {
//            throw Message.getSQLException(ErrorCode.VIEW_NOT_FOUND_1, viewName);
//        }
//        TableView view = (TableView) tableView;
//        command.setView(view);
//        read("RECOMPILE");
//        return command;
        return null;
    }

    private Prepared parseAlterSequence() throws SQLException {
        return null;
    }

    private Prepared parseAlterUser() throws SQLException {
//        String userName = readUniqueIdentifier();
//        if (readIf("SET")) {
//            AlterUser command = new AlterUser(session);
//            command.setType(AlterUser.SET_PASSWORD);
//            command.setUser(database.getUser(userName));
//            if (readIf("PASSWORD")) {
//                command.setPassword(readExpression());
//            } else if (readIf("SALT")) {
//                command.setSalt(readExpression());
//                read("HASH");
//                command.setHash(readExpression());
//            } else {
//                throw getSyntaxError();
//            }
//            return command;
//        } else if (readIf("RENAME")) {
//            read("TO");
//            AlterUser command = new AlterUser(session);
//            command.setType(AlterUser.RENAME);
//            command.setUser(database.getUser(userName));
//            String newName = readUniqueIdentifier();
//            command.setNewName(newName);
//            return command;
//        } else if (readIf("ADMIN")) {
//            AlterUser command = new AlterUser(session);
//            command.setType(AlterUser.ADMIN);
//            User user = database.getUser(userName);
//            command.setUser(user);
//            if (readIf("TRUE")) {
//                command.setAdmin(true);
//            } else if (readIf("FALSE")) {
//                command.setAdmin(false);
//            } else {
//                throw getSyntaxError();
//            }
//            return command;
//        }
//        throw getSyntaxError();
        return null;
    }

    private void readIfEqualOrTo() throws SQLException {
        if (!readIf("=")) {
            readIf("TO");
        }
    }

    private Prepared parseSet() throws SQLException {
        return null;
    }

    private Prepared parseSetCollation() throws SQLException {
        return null;


    }

    private Prepared parseRunScript() throws SQLException {
        return null;
    }

    private Prepared parseScript() throws SQLException {
        return null;
    }

    private Table readTableOrView() throws SQLException {
        return readTableOrView(readIdentifierWithSchema(null));
    }

    private Table readTableOrView(String tableName) throws SQLException {
//        // same algorithm than readSequence
//        if (schemaName != null) {
//            return getSchema().getTableOrView(session, tableName);
//        }
        Table table = database.getMainSchema(
                //session.getCurrentSchemaName()
                ).findTableOrView(tableName);
        if (table != null) {
            return table;
        }
//        throw new RuntimeException("Unable to find table " + tableName);
//        String[] schemaNames = session.getSchemaSearchPath();
//        for (int i = 0; schemaNames != null && i < schemaNames.length; i++) {
//            Schema s = database.getSchema(schemaNames[i]);
//            table = s.findTableOrView(session, tableName);
//            if (table != null) {
//                return table;
//            }
//        }
        throw new SQLException("Unable to find table " + tableName);
    }

    private Sequence findSequence(String schemaName, String sequenceName) throws SQLException {
//        Sequence sequence = database.getSchema(schemaName).findSequence(sequenceName);
//        if (sequence != null) {
//            return sequence;
//        }
//        String[] schemaNames = session.getSchemaSearchPath();
//        for (int i = 0; schemaNames != null && i < schemaNames.length; i++) {
//            Schema s = database.getSchema(schemaNames[i]);
//            sequence = s.findSequence(sequenceName);
//            if (sequence != null) {
//                return sequence;
//            }
//        }
        return null;
    }

    private Sequence readSequence() throws SQLException {
//        // same algorithm than readTableOrView
//        String sequenceName = readIdentifierWithSchema(null);
//        if (schemaName != null) {
//            return getSchema().getSequence(sequenceName);
//        }
//        Sequence sequence = findSequence(session.getCurrentSchemaName(), sequenceName);
//        if (sequence != null) {
//            return sequence;
//        }
        throw Message.getSQLException(ErrorCode.SEQUENCE_NOT_FOUND_1, "sequenceName");
    }

    private Prepared parseAlterTable() throws SQLException {
        Table table = readTableOrView();
        if (readIf("ADD")) {
            Prepared command = parseAlterTableAddConstraintIf(table.getName(), table.getSchema());
            if (command != null) {
                return command;
            }
            return parseAlterTableAddColumn(table);
        } else if (readIf("SET")) {
            return null;
        } else if (readIf("RENAME")) {
            read("TO");
            //String newName = readIdentifierWithSchema(table.getSchema().getName());
            //checkSchema(table.getSchema());
            return null;
        } else if (readIf("DROP")) {
            if (readIf("CONSTRAINT")) {
                return null;
            } else if (readIf("PRIMARY")) {
                read("KEY");

                return null;
            } else {
                readIf("COLUMN");
                return null;
            }
        } else if (readIf("ALTER")) {
            readIf("COLUMN");
            String columnName = readColumnIdentifier();
            Column column = null; //table.getColumn(columnName);
            if (readIf("RENAME")) {
                read("TO");
                return null;
            } else if (readIf("SET")) {
                if (readIf("DATA")) {
                    // Derby compatibility
                    read("TYPE");
                    Column newColumn = parseColumnForTable(columnName);
                    return null;
                }
                return null;
            } else if (readIf("RESTART")) {
                readIf("WITH");
                Expression start = readExpression();
                return null;
            } else if (readIf("SELECTIVITY")) {
                return null;
            } else {
                Column newColumn = parseColumnForTable(columnName);
                return null;
            }
        }
        throw getSyntaxError();
    }

    private Prepared parseAlterTableAddColumn(Table table) throws SQLException {
        return null;
    }

    private int parseAction() throws SQLException {
       return 0;
    }

    private Prepared parseAlterTableAddConstraintIf(String tableName, Schema schema) throws SQLException {
        String constraintName = null, comment = null;
        boolean ifNotExists = false;
        if (readIf("CONSTRAINT")) {
            ifNotExists = readIfNoExists();
            constraintName = readIdentifierWithSchema(schema.getName());
            checkSchema(schema);
            comment = readCommentIf();
        }
        if (readIf("PRIMARY")) {
            read("KEY");
            return null;
        } else if (false && (readIf("INDEX") || readIf("KEY"))) {
            // MySQL
            return null;
        }
        Prepared command;
        if (readIf("CHECK")) {
            command = null; //new AlterTableAddConstraint(session, schema, ifNotExists);
            //command.setType(AlterTableAddConstraint.CHECK);
            //command.setCheckExpression(readExpression());
        } else if (readIf("UNIQUE")) {
            readIf("KEY");
            readIf("INDEX");
            command = null; //new AlterTableAddConstraint(session, schema, ifNotExists);
            //command.setType(AlterTableAddConstraint.UNIQUE);
            if (!readIf("(")) {
                constraintName = readUniqueIdentifier();
                read("(");
            }
            //command.setIndexColumns(parseIndexColumnList());
            if (readIf("INDEX")) {
                String indexName = readIdentifierWithSchema();

            }
        } else if (readIf("FOREIGN")) {
            command = null;
            read("KEY");
            read("(");
            //command.setIndexColumns(parseIndexColumnList());
            if (readIf("INDEX")) {
                String indexName = readIdentifierWithSchema();

            }
            read("REFERENCES");
            parseReferences(command, schema, tableName);
        } else {
            if (constraintName != null) {
                throw getSyntaxError();
            }
            return null;
        }
        if (readIf("NOCHECK")) {
            //command.setCheckExisting(false);
        } else {
            readIf("CHECK");
            //command.setCheckExisting(true);
        }
        //command.setTableName(tableName);
        //command.setConstraintName(constraintName);
        //command.setComment(comment);
        return command;
    }

    private void parseReferences(Prepared command, Schema schema, String tableName) throws SQLException {
//        if (readIf("(")) {
//            command.setRefTableName(schema, tableName);
//            command.setRefIndexColumns(parseIndexColumnList());
//        } else {
//            String refTableName = readIdentifierWithSchema(schema.getName());
//            command.setRefTableName(getSchema(), refTableName);
//            if (readIf("(")) {
//                command.setRefIndexColumns(parseIndexColumnList());
//            }
//        }
//        if (readIf("INDEX")) {
//            String indexName = readIdentifierWithSchema();
//
//        }
//        while (readIf("ON")) {
//            if (readIf("DELETE")) {
//                command.setDeleteAction(parseAction());
//            } else {
//                read("UPDATE");
//                command.setUpdateAction(parseAction());
//            }
//        }
//        if (readIf("NOT")) {
//            read("DEFERRABLE");
//        } else {
//            readIf("DEFERRABLE");
//        }
    }

    private Prepared parseCreateLinkedTable(boolean temp, boolean globalTemp, boolean force) throws SQLException {
        return null;
    }

    private Prepared parseCreateTable(boolean temp, boolean globalTemp, boolean persistIndexes) throws SQLException {
        return null;
    }

    private String getCompareType(int tokenType) {
        switch (tokenType) {
        case EQUAL:
            return Operator.EQUAL;
        case BIGGER_EQUAL:
            return Operator.BIGGER_EQUAL;
        case BIGGER:
            return Operator.BIGGER;
        case SMALLER:
            return Operator.SMALLER;
        case SMALLER_EQUAL:
            return Operator.SMALLER_EQUAL;
        case NOT_EQUAL:
            return Operator.NOT_EQUAL;
        default:
            return null;
        }
    }

    public void setRightsChecked(boolean rightsChecked) {
        this.rightsChecked = rightsChecked;
    }

    /**
     * Parse a SQL code snippet that represents an expression.
     *
     * @param sql the code snippet
     * @return the expression object
     * @throws SQLException if the code snippet could not be parsed
     */
    public Expression parseExpression(String sql) throws SQLException {
        parameters = ObjectArray.newInstance();
        initialize(sql);
        read();
        return readExpression();
    }

}
