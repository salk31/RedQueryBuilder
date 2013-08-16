package com.redspr.redquerybuilder.core.client.table;

import java.util.ArrayList;
import java.util.List;

import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.expression.Comparison;
import com.redspr.redquerybuilder.core.client.expression.Expression;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Constraint;
import com.redspr.redquerybuilder.core.shared.meta.ConstraintReferential;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.Table;

// XXX dodgy name
public class JoinHelper {

    // private final Session session;

    private TableFilter parent;
    private final TableFilter child;
    private Constraint constraint;
    private final Expression joinCondition;

    // XXX not valid till isSimple called
    public TableFilter getParent() {
        return parent;
    }

    // XXX not valid till isSimple called
    public Constraint getConstraint() {
        return constraint;
    }

    public JoinHelper(TableFilter child2, Expression joinCondition2) {
        this.child = child2;
        this.joinCondition = joinCondition2;
    }

    private static boolean match(ConstraintReferential a,
            ConstraintReferential b) {
        if (match(a, b.getTable(), b.getColumns(), b.getRefTable(), b.getRefColumns())) {
            return true;
        }

        if (match(a, b.getRefTable(), b.getRefColumns(), b.getTable(), b.getColumns())) {
            return true;
        }
        return false;
    }

    private static boolean match(ConstraintReferential a, Table btl, Column[] bc, Table btr, Column[] bcr) {
        if (a.getTable() != btl) {
            return false;
        }
        if (a.getRefTable() != btr) {
            return false;
        }
        if (a.getColumns().length != bc.length) {
            return false;
        }
        // TODO 90 dont want to care about order
        for (int i = 0; i < bc.length; i++) {
            if (a.getColumns()[i] != bc[i]) {
                return false;
            }
            if (a.getRefColumns()[i] != bcr[i]) {
                return false;
            }
        }
        return true;
    }

    public static TableFilter getOrCreateFor(Session session,
            ConstraintReferential ref) {

        for (TableFilter tf : session.getFilters()) {
            // XXX not very efficient as know the ref wanted?
            JoinHelper jh = new JoinHelper(tf, tf.getJoinCondition());

            if ((jh.isCartesian() && tf.getTable() == ref.getRefTable())
                    || (jh.isSimple() && jh.getConstraint() == ref)) {
                return tf;
            }
        }

        TableFilter tableFilter = session
                .getTableFilter(ref.getTable());

        TableFilter refTableFilter = session.createTableFilter(ref
                .getRefTable());

        Comparison joinCond = new Comparison(session, Operator.EQUAL,
                new ExpressionColumn(session, null, tableFilter.getAlias(), ref
                        .getColumns()[0].getName()), new ExpressionColumn(
                        session, null, refTableFilter.getAlias(), ref
                                .getRefColumns()[0].getName()));
        tableFilter.addJoin(refTableFilter, false, joinCond);
        return refTableFilter;
    }

    // XXX better name "forChild"
    public static JoinHelper getParent(TableFilter tf) {
        // for (TableFilter tf2 : tf.getSession().getFilters()) {
        // TableFilter child2 = tf2.getJoin();
        Expression joinCondition = tf.getJoinCondition();
        JoinHelper thing = new JoinHelper(tf, joinCondition);

        if (thing.isSimple()) {
            return thing;
        }
        // }
        return null;
    }

    private boolean foo(Expression exp, List<Column> parentCols,
            List<Column> childCols) {
        if (exp instanceof Comparison) {
            return foo((Comparison) exp, parentCols, childCols);
        }

        return false;
    }

    private boolean foo(Comparison c, List<Column> parentCols,
            List<Column> childCols) {
        if (!Operator.EQUAL.equals(c.getOperatorName())) {
            return false;
        }
        if (!(c.getLeft() instanceof ExpressionColumn)) {
            return false;
        }
        if (!(c.getRight() instanceof ExpressionColumn)) {
            return false;
        }
        ExpressionColumn left = (ExpressionColumn) c.getLeft();
        ExpressionColumn right = (ExpressionColumn) c.getRight();
        if (left.getTableFilter() == child) {
            foo(right.getTableFilter());
            childCols.add(left.getColumn());
            parentCols.add(right.getColumn());
        } else if (right.getTableFilter() == child) {
            foo(left.getTableFilter());
            childCols.add(right.getColumn());
            parentCols.add(left.getColumn());
        } else {
            return false;
        }
return true;
    }

    private boolean foo(TableFilter parent2) {
        if (parent == null) {
            parent = parent2;
        } else {
            if (parent != parent2) {
                return false;
            }
        }
        return true;
    }

    private boolean isCartesian() {
        isSimple();
        return getParent() == null;
    }

    private boolean isSimple() {
        List<Column> parentCols = new ArrayList();
        List<Column> childCols = new ArrayList();
        if (
           !foo(joinCondition, parentCols, childCols)) {

            return false;
        }
        ConstraintReferential cand = new ConstraintReferential("test", child
                .getTable());
        cand.setRefTable(parent.getTable());
        cand.setColumns(childCols.toArray(new Column[0]));
        cand.setRefColumns(parentCols.toArray(new Column[0]));

        for (Constraint c : parent.getTable().getConstraints()) {
            if (c instanceof ConstraintReferential) {
                ConstraintReferential cr = (ConstraintReferential) c;
                if (match(cand, cr)) {
                    this.constraint = cr;
                    return true;
                }
            }
        }

        return false;
    }
}
