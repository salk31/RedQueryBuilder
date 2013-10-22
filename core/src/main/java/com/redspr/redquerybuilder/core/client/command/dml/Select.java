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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.redspr.redquerybuilder.core.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.engine.ColumnResolver;
import com.redspr.redquerybuilder.core.client.engine.DirtyEvent;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.engine.TableEvent;
import com.redspr.redquerybuilder.core.client.expression.Comparison;
import com.redspr.redquerybuilder.core.client.expression.ConditionAndOr;
import com.redspr.redquerybuilder.core.client.expression.Expression;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.CommandListBox;
import com.redspr.redquerybuilder.core.client.util.CommandWithLabel;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.client.util.StatementBuilder;
import com.redspr.redquerybuilder.core.client.util.XWidget;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Table;

public class Select extends Query implements  ColumnResolver {

    private final Button add = new Button("Add condition");

    private final XWidget<Expression> xcondition = new XWidget<Expression>();

    private HashMap<Expression, Object> currentGroup;

    private int currentGroupRowId;

    private ObjectArray<Expression> expressions;
    private final ObjectArray<TableFilter> filters = ObjectArray.newInstance();
    // private ObjectArray<SelectOrderBy> orderList;
    private ObjectArray<Expression> group;
    private boolean[] groupByExpression;
    private int[] groupIndex;
    private Expression having;

    private boolean isQuickAggregateQuery;
    private final Session session;
    private int visibleColumnCount;

    private final VerticalPanel vp = new VerticalPanel();

    private final CommandListBox what;

    public Select(final Session session2) {
        super(session2);
        session = session2;
        session.setSelect(this);

        initWidget(vp);

        what = new CommandListBox(this);
        what.addStyleName("rqbWhat");  // XXX to be removed
        what.addStyleName("rqbFrom");
        what.setVisible(session.getConfig().getFrom().isVisible());
        vp.add(what);

        vp.add(add);
        add.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addFirstCondition();
                fireDirty();
            }
        });
        List items = new ArrayList();
        CommandWithLabel prompt = new PromptCommand();
        what.setValue(prompt);
        items.add(prompt);

        // XXX 00 What is SQL 92 case sensitivity rule

        for (Table t : session.getDatabase().getMainSchema().getTables()) {
            if (!t.isHidden()) {
                items.add(new TableCommand(t));
            }
        }

        what.setAcceptableValues(items);

        vp.add(xcondition);
    }

    // XXX scope for testing
    public Comparison addFirstCondition() {
        Comparison c = new Comparison(session);
        addCondition(c);
        return c;
    }

    class TableCommand extends CommandWithLabel {
        private final Table table;
        TableCommand(Table t) {
            super(t);
            table = t;
        }
        @Override
        public void execute() {
            updateTable(table);
        }
    }

    class PromptCommand extends CommandWithLabel {
        PromptCommand() {
            super("Please select");
        }

        @Override
        public void execute() {
            updateTable(null);
        }
    }


    /**
     * Add a condition to the list of conditions.
     *
     * @param cond
     *            the condition to add
     */
    public void addCondition(Expression cond) {
        Expression condition = getCondition();
        if (condition == null) {
            condition = cond;
        } else {
            condition = new ConditionAndOr(session, ConditionAndOr.AND, cond, condition);
        }
        setCondition(condition);
    }

    private void setCondition(Expression condition) {
        xcondition.setValue(condition);
    }

    /**
     * Add a table to the query.
     *
     * @param filter
     *            the table to add
     * @param isTop
     *            if the table can be the first table in the query plan
     */
    public void addTableFilter(TableFilter filter, boolean isTop) {
        // Oracle doesn't check on duplicate aliases
        // String alias = filter.getAlias();
        // if(filterNames.contains(alias)) {
        // throw Message.getSQLException(
        // ErrorCode.DUPLICATE_TABLE_ALIAS, alias);
        // }
        // filterNames.add(alias);

        filters.add(filter);

    }

    public int getColumnCount() {
        return visibleColumnCount;
    }

    public Expression getCondition() {
        return xcondition.getValue();
    }

    public HashMap<Expression, Object> getCurrentGroup() {
        return currentGroup;
    }

    public int getCurrentGroupRowId() {
        return currentGroupRowId;
    }

    public ObjectArray<Expression> getExpressions() {
        return expressions;
    }

    public ObjectArray<TableFilter> getFilters() {
        return filters;
    }

    public String getSQL(List args) {

        StatementBuilder buff = new StatementBuilder("SELECT ");
        // if (distinct) {
        // buff.append("DISTINCT ");
        // }
        if (expressions != null) {
         for (int i = 0; i < expressions.size(); i++) {
         buff.appendExceptFirst(", ");
         buff.append(expressions.get(i).getSQL(args));
         }
        }
        buff.append("\nFROM ");
        TableFilter filter = null;
        if (filters.size() > 0) {
            filter = filters.get(0);
        }
        if (filter != null) {
            buff.resetCount();
            int i = 0;
            do {
            buff.appendExceptFirst("\n");
            buff.append(filter.getSQL(i++ > 0, args));
            filter = filter.getJoin();
            } while (filter != null);
        } else {
         buff.resetCount();
         int i = 0;
         for (TableFilter f : filters) {
         buff.appendExceptFirst("\n");
         buff.append(f.getSQL(i++ > 0, args));
         }
         }
        if (xcondition.getValue() != null) {
            buff.append("\nWHERE ").append(xcondition.getValue().getSQL(args));
        }
        // if (groupIndex != null) {
        // buff.append("\nGROUP BY ");
        // buff.resetCount();
        // for (int gi : groupIndex) {
        // Expression g = exprList[gi];
        // g = g.getNonAliasExpression();
        // buff.appendExceptFirst(", ");
        // buff.append(StringUtils.unEnclose(g.getSQL()));
        // }
        // }
        // if (group != null) {
        // buff.append("\nGROUP BY ");
        // buff.resetCount();
        // for (Expression g : group) {
        // buff.appendExceptFirst(", ");
        // buff.append(StringUtils.unEnclose(g.getSQL()));
        // }
        // }
        // if (having != null) {
        // // could be set in addGlobalCondition
        // // in this case the query is not run directly, just getPlanSQL is
        // // called
        // Expression h = having;
        // buff.append("\nHAVING ").append(StringUtils.unEnclose(h.getSQL()));
        // } else if (havingIndex >= 0) {
        // Expression h = exprList[havingIndex];
        // buff.append("\nHAVING ").append(StringUtils.unEnclose(h.getSQL()));
        // }
        // if (sort != null) {
        // buff.append("\nORDER BY ").append(sort.getSQL(exprList,
        // visibleColumnCount));
        // }
        // if (orderList != null) {
        // buff.append("\nORDER BY ");
        // buff.resetCount();
        // for (SelectOrderBy o : orderList) {
        // buff.appendExceptFirst(", ");
        // buff.append(StringUtils.unEnclose(o.getSQL()));
        // }
        // }
        // if (limitExpr != null) {
        // buff.append("\nLIMIT ").append(StringUtils.unEnclose(limitExpr.getSQL()));
        // if (offsetExpr != null) {
        // buff.append(" OFFSET ").append(StringUtils.unEnclose(offsetExpr.getSQL()));
        // }
        // }
        // if (isForUpdate) {
        // buff.append("\nFOR UPDATE");
        // }
        // if (isQuickAggregateQuery) {
        // buff.append("\n/* direct lookup */");
        // }
        // if (isDistinctQuery) {
        // buff.append("\n/* distinct */");
        // }
        // if (sortUsingIndex) {
        // buff.append("\n/* index sorted */");
        // }
        // if (isGroupQuery) {
        // if (isGroupSortedQuery) {
        // buff.append("\n/* group sorted */");
        // }
        // }
        return buff.toString();
    }

   // public ListBox getWhat() {
   //     return what;
   // }

    // public HashSet<Table> getTables() {
    // HashSet<Table> set = New.hashSet();
    // for (TableFilter filter : filters) {
    // set.add(filter.getTable());
    // }
    // return set;
    // }

    /**
     * Check if this is an aggregate query with direct lookup, for example a
     * query of the type SELECT COUNT(*) FROM TEST or SELECT MAX(ID) FROM TEST.
     *
     * @return true if a direct lookup is possible
     */
    public boolean isQuickAggregateQuery() {
        return isQuickAggregateQuery;
    }

    @Override
    public BaseSqlWidget remove(Expression e) {
        if (getCondition() == e) {
            setCondition(null);
            return this;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void replace(Expression e0, Expression e1) {
        if (getCondition() == e0) {
            setCondition(e1);
        }
    }

    public void setExpressions(ObjectArray<Expression> p) {
        this.expressions = p;
    }

    // public void mapColumns(ColumnResolver resolver, int level) throws
    // SQLException {
    // for (Expression e : expressions) {
    // e.mapColumns(resolver, level);
    // }
    // if (condition != null) {
    // condition.mapColumns(resolver, level);
    // }
    // }
    //
    // public void setEvaluatable(TableFilter tableFilter, boolean b) {
    // for (Expression e : expressions) {
    // e.setEvaluatable(tableFilter, b);
    // }
    // if (condition != null) {
    // condition.setEvaluatable(tableFilter, b);
    // }
    // }

    public void setGroupBy(ObjectArray<Expression> p) {
        this.group = p;
    }

    public void setHaving(Expression p) {
        this.having = p;
    }

    public void updateTable(Table tt) {
        if (tt == null) {
            filters.clear();
        } else if (getFilters().size() == 0) {
            TableFilter filter = new TableFilter(session, tt, TableFilter
                    .newAlias(), Select.this);
            addTableFilter(filter, true); // XXX really is true?
        } else {
            getFilters().get(0).setTable(tt);
        }
        expressions = null; // XXX 03 bit harsh?
        setCondition(null);

        session.getMsgBus().fireEvent(new TableEvent());
    }

    @Override
    public void onDirty(DirtyEvent e) {
        garbageCollectFilters();

        add.setVisible(getCondition() == null && this.getFilters().size() > 0);

        if (getFilters().size() > 0) {
            what.setValue(new TableCommand(getFilters().get(0).getTable()));
        }
    }

    public void garbageCollectFilters() {
        if (filters.size() > 1) {
            // XXX identity?
            final Set<TableFilter> used = new HashSet<TableFilter>();
            used.add(null);
            Callback counter = new BaseSqlWidget.Callback() {
                @Override
                public void handle(BaseSqlWidget w) {
                    if (w instanceof ExpressionColumn) {
                        ExpressionColumn ec = (ExpressionColumn) w;
                        TableFilter tf = ec.getTableFilter();
                        if (used.add(tf)) {
                            if (tf.getJoinCondition() != null) {
                                tf.getJoinCondition().traverse(this);
                            }
                        }
                    }
                }
            };

            if (getCondition() != null) {
                getCondition().traverse(counter);
            }

            boolean removed;
            do {
                removed = false;
                TableFilter prev = filters.get(0);
                TableFilter x = prev.getJoin();
                while (x != null) {
                    if (!used.contains(x)) {
                        // look for used in all other filters?
                        int i = filters.indexOf(x);
                        if (i > 0) {
                            filters.remove(i);
                        }
                            // XXX why is this the case?
                            // just when comma seperated?
                        prev.setJoin(x.getJoin());
                        removed = true;
                    }
                    prev = x;
                    x = x.getJoin();
                }
            } while (removed);

        }
    }

    @Override
    public void traverse(Callback callback) {
        super.traverse(callback);
        Expression condition = getCondition();
        if (condition != null) {
            condition.traverse(callback);
        }
    }

    @Override
    public Column resolveColumn(String alias, String columnName) {
        for (TableFilter tf : getFilters()) {
            if (alias == null || alias.equals(tf.getAlias())) {
               Column c = tf.getTable().getColumn(columnName);
               if (c != null) {
                   return c;
               }
               // XXX if alias != null should blowup?
               // XXX should blowup if find more than one?
            }

            //if (alias.equals(tf.getAlias())) {
            //    return tf.getTable().getColumn(columnName);
            //}
        }
        // XXX log? Window.alert("Unable to find " + alias + "." + columnName);
        return null;
    }

    public TableFilter getTableFilter(String alias) {
        if (alias == null) {
            return getFilters().get(0);
        }

        for (TableFilter tf : getFilters()) {
            if (alias.equals(tf.getAlias())) {
                return tf;
            }
        }
        return null;
    }

    // public void addGlobalCondition(Parameter param, int columnId, int
    // comparisonType) throws SQLException {
    // addParameter(param);
    // Expression col = expressions.get(columnId);
    // col = col.getNonAliasExpression();
    // Expression comp = new Comparison(session, comparisonType, col, param);
    // comp = comp.optimize(session);
    // boolean addToCondition = true;
    // if (isGroupQuery) {
    // addToCondition = false;
    // for (int i = 0; groupIndex != null && i < groupIndex.length; i++) {
    // if (groupIndex[i] == columnId) {
    // addToCondition = true;
    // break;
    // }
    // }
    // if (!addToCondition) {
    // if (havingIndex >= 0) {
    // having = expressions.get(havingIndex);
    // }
    // if (having == null) {
    // having = comp;
    // } else {
    // having = new ConditionAndOr(ConditionAndOr.AND, having, comp);
    // }
    // }
    // }
    // if (addToCondition) {
    // if (condition == null) {
    // condition = comp;
    // } else {
    // condition = new ConditionAndOr(ConditionAndOr.AND, condition, comp);
    // }
    // }
    // }

    // public String getFirstColumnAlias(Session s) {
    // if (SysProperties.CHECK) {
    // if (visibleColumnCount > 1) {
    // Message.throwInternalError("" + visibleColumnCount);
    // }
    // }
    // Expression expr = expressions.get(0);
    // if (expr instanceof Alias) {
    // return expr.getAlias();
    // }
    // Mode mode = s.getDatabase().getMode();
    // String name = s.getNextSystemIdentifier(getSQL());
    // expr = new Alias(expr, name, mode.aliasColumnName);
    // expressions.set(0, expr);
    // return expr.getAlias();
    // }
    //

}
