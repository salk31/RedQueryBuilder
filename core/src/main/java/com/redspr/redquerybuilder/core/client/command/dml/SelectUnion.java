/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 *
 * Nicolas Fortin, Atelier SIG, IRSTV FR CNRS 24888
 * Support for the operator "&&" as an alias for SPATIAL_INTERSECTS
 */
package com.redspr.redquerybuilder.core.client.command.dml;

import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.expression.Expression;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;


/**
 * Represents a union SELECT statement.
 */
public class SelectUnion extends Query {

    /**
     * The type of a UNION statement.
     */
    public static final int UNION = 0;

    /**
     * The type of a UNION ALL statement.
     */
    public static final int UNION_ALL = 1;

    /**
     * The type of an EXCEPT statement.
     */
    public static final int EXCEPT = 2;

    /**
     * The type of an INTERSECT statement.
     */
    public static final int INTERSECT = 3;

    private int unionType;
    private final Query left;

    private Query right;
    private ObjectArray<Expression> expressions;
   // private ObjectArray<SelectOrderBy> orderList;
  // SortOrder sort;
    private boolean distinct;
    private boolean isPrepared, checkInit;
    private boolean isForUpdate;

    public SelectUnion(Session session, Query query) {
        super(session);
        this.left = query;
    }

    public void setUnionType(int type) {
        this.unionType = type;
    }

    public void setRight(Query select) {
        right = select;
    }


//    public void setOrder(ObjectArray<SelectOrderBy> order) {
//        orderList = order;
//    }

//    private Value[] convert(Value[] values, int columnCount) throws SQLException {
//        for (int i = 0; i < columnCount; i++) {
//            Expression e = expressions.get(i);
//           // values[i] = values[i].convertTo(e.getType());
//        }
//        return values;
//    }




//    public HashSet<Table> getTables() {
//        HashSet<Table> set = left.getTables();
//        set.addAll(right.getTables());
//        return set;
//    }

    public void setDistinct(boolean b) {
        distinct = b;
    }

    public ObjectArray<Expression> getExpressions() {
        return expressions;
    }

//    public void mapColumns(ColumnResolver resolver, int level) throws SQLException {
//        left.mapColumns(resolver, level);
//        right.mapColumns(resolver, level);
//    }
//
//    public void setEvaluatable(TableFilter tableFilter, boolean b) {
//        left.setEvaluatable(tableFilter, b);
//        right.setEvaluatable(tableFilter, b);
//    }

//    public void addGlobalCondition(Parameter param, int columnId, int comparisonType) throws SQLException {
//        addParameter(param);
//        switch (unionType) {
//        case UNION_ALL:
//        case UNION:
//        case INTERSECT: {
//            left.addGlobalCondition(param, columnId, comparisonType);
//            right.addGlobalCondition(param, columnId, comparisonType);
//            break;
//        }
//        case EXCEPT: {
//            left.addGlobalCondition(param, columnId, comparisonType);
//            break;
//        }
//        default:
//            Message.throwInternalError("type=" + unionType);
//        }
//    }

//    public String getPlanSQL() {
//        StringBuilder buff = new StringBuilder();
//        buff.append('(').append(StringUtils.unEnclose(left.getPlanSQL())).append(')');
//        switch (unionType) {
//        case UNION_ALL:
//            buff.append("UNION ALL ");
//            break;
//        case UNION:
//            buff.append("UNION ");
//            break;
//        case INTERSECT:
//            buff.append("INTERSECT ");
//            break;
//        case EXCEPT:
//            buff.append("EXCEPT ");
//            break;
//        default:
//            Message.throwInternalError("type=" + unionType);
//        }
//        buff.append('(').append(StringUtils.unEnclose(right.getPlanSQL())).append(')');
//        Expression[] exprList = expressions.toArray(new Expression[expressions.size()]);
//        if (sort != null) {
//            buff.append(" ORDER BY ").append(sort.getSQL(exprList, exprList.length));
//        }
//        if (limitExpr != null) {
//            buff.append(" LIMIT ").append(StringUtils.unEnclose(limitExpr.getSQL()));
//            if (offsetExpr != null) {
//                buff.append(" OFFSET ").append(StringUtils.unEnclose(offsetExpr.getSQL()));
//            }
//        }
//        if (isForUpdate) {
//            buff.append(" FOR UPDATE");
//        }
//        return buff.toString();
//    }




    public String getFirstColumnAlias(Session s) {
        return null;
    }

}
