package com.redspr.redquerybuilder.core.client.table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.expression.ConditionAndOr;
import com.redspr.redquerybuilder.core.client.expression.Expression;
import com.redspr.redquerybuilder.core.client.util.StringUtils;
import com.redspr.redquerybuilder.core.shared.meta.Table;

public class TableFilter {
    private Table table = null;
    private final String alias;

    private Expression joinCondition;

    private Expression filterCondition;
    private TableFilter join;

    private boolean outerJoin;
    private static Set<String> usedAliases = new HashSet<String>();
    private final Session session;

    public TableFilter(Session session2, Table table2, String alias2, Select select) {
        this.session = session2;
        this.table = table2;
        this.alias = alias2;

        if (table == null) {
            throw new RuntimeException("Table must not be null");
        }

        if (alias != null) {
            usedAliases.add(alias.toUpperCase());
        }
    }

    public static String newAlias() {
        int i = usedAliases.size();
        while (usedAliases.contains("X" + i)) {
            i++;
        }
        return "x" + i;
    }

    public static void resetAlias() {
        usedAliases.clear();
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table p) {
        if (p == null) {
            throw new RuntimeException("Table must not be null");
        }
        this.table = p;
    }

    public String getAlias() {
       // if (alias == null) alias = newAlias(); // XXX not just do early?
        return alias;
    }

    public String getTableAlias() {
        if (alias != null) {
            return alias;
        }
        return table.getName();
    }

    public void addJoin(TableFilter filter, boolean outer, Expression on) {
        if (filter == this) {
            throw new IllegalArgumentException("Reference to self");
        }

                if (join == null) {
                    this.join = filter;
                    filter.outerJoin = outer;
                    if (outer) {
                        // convert all inner joins on the right hand side to outer joins
                        TableFilter f = filter.join;
                        while (f != null) {
                            f.outerJoin = true;
                            f = f.join;
                        }
                   }
                   if (on != null) {
                       filter.mapAndAddFilter(on);
                   }
               } else {
                   join.addJoin(filter, outer, on);
               }
           }

    public void mapAndAddFilter(Expression on) {
        // don't know what this is for on.mapColumns(this, 0);
        addFilterCondition(on, true);
        // on.createIndexConditions(session, this);
        if (join != null) {
            join.mapAndAddFilter(on);
        }
    }

    public TableFilter getJoin() {
        return join;
    }

    public String getSQL(boolean isJoin, List args) {

        StringBuilder buff = new StringBuilder();
        if (isJoin) {
            if (outerJoin) {
                buff.append("LEFT OUTER JOIN ");
            } else {
                buff.append("INNER JOIN ");
            }
        }
        buff.append(table.getSQL());
        if (alias != null) {
            buff.append(' ').append(Session.quoteIdentifier(alias));
        }

        if (isJoin) {
            buff.append(" ON ");
            if (joinCondition == null) {
                // need to have a ON expression, otherwise the nesting is
                // unclear
                buff.append("1=1");
            } else {
                buff.append(StringUtils.unEnclose(joinCondition.getSQL(args)));
            }
        }

        return buff.toString();
    }

    public void addFilterCondition(Expression condition, boolean isJoin) {
        if (isJoin) {
            if (joinCondition == null) {
                joinCondition = condition;
            } else {
                joinCondition = new ConditionAndOr(session, ConditionAndOr.AND,
                        joinCondition, condition);
            }
        } else {
            if (filterCondition == null) {
                filterCondition = condition;
            } else {
                filterCondition = new ConditionAndOr(session,
                        ConditionAndOr.AND, filterCondition, condition);
            }
        }
    }

     public Expression getJoinCondition() {
         return joinCondition;
     }

     public void removeJoinCondition() {
         this.joinCondition = null;
     }

     public void removeJoin() {
         this.join = null;
     }

     public Session getSession() {
         return session;
     }

     public boolean isJoinOuter() {
         return outerJoin; // XXX
     }

     // XXX not sure about this
     public void setJoin(TableFilter p) {
         this.join = p;
     }

     @Override
     public String toString() {
         return table.getName() + " " + this.alias;
     }
}
