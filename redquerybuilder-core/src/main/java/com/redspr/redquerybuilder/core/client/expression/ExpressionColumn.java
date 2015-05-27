package com.redspr.redquerybuilder.core.client.expression;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.engine.TableEvent;
import com.redspr.redquerybuilder.core.client.engine.TableEventHandler;
import com.redspr.redquerybuilder.core.client.table.JoinHelper;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.CommandListBox;
import com.redspr.redquerybuilder.core.client.util.CommandWithLabel;
import com.redspr.redquerybuilder.core.client.util.StringUtils;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Constraint;
import com.redspr.redquerybuilder.core.shared.meta.ConstraintReferential;
import com.redspr.redquerybuilder.core.shared.meta.Database;

/**
 * A expression that represents a column of a table or view.
 */
public class ExpressionColumn extends Expression implements TableEventHandler {
    private Database database;
    private final String schemaName;
    private String tableAlias;
    private String columnName;

    private final HorizontalPanel hp = new HorizontalPanel();

    public ExpressionColumn(Session session2, String schemaName2,
            String tableAlias2, String columnName2) {
        super(session2);
        this.schemaName = schemaName2;
        this.tableAlias = tableAlias2;
        this.columnName = columnName2;

        initWidget(hp);

        reg2 = getSession().getMsgBus().addHandler(TableEvent.TYPE, this);
    }

    public void selectConstraintRef(ConstraintReferential ref) {
        TableFilter targetTf = JoinHelper.getOrCreateFor(getSession(), ref);
        this.tableAlias = targetTf.getAlias();
        this.columnName = targetTf.getTable().getColumns().iterator().next()
                .getName();

    }

    public void updateColumn(String alias, Column col2) {
        this.tableAlias = alias;
        this.columnName = col2.getName();
    }

    private final HandlerRegistration reg2;

    @Override
    public void onUnload() {
        super.onUnload();
        if (reg2 != null) {
            reg2.removeHandler();
        }
    }

    public Column getColumn() {
        return getSession().resolveColumn(tableAlias, getColumnName());
    }

    public String getColumnName() {
        return columnName;
    }

    public String getQualifiedColumnName() {
        if (tableAlias == null) {
            return getColumnName();
        }
        return tableAlias + "." + getColumnName();
    }

    @Override
    public void onDirty() {
        hp.clear();

        TableFilter tf = null;
        for (TableFilter tf2 : getSession().getFilters()) {
            if (StringUtils.equals(tableAlias, tf2.getAlias())) {
                tf = tf2;
                break;
            } else if (tf == null) {
                tf = tf2;
            }
        }

        Object hotValue = getColumn();

        Command hotCommand = null;
        while (tf != null) {
            final CommandListBox ght = new CommandListBox(this);
            hp.insert(ght, 0);
            List<Command> items = new ArrayList();
//           hotCommand = new ClearCommand();
//            items.add(hotCommand);

            for (Constraint c : tf.getTable().getConstraints()) {
                if (c instanceof ConstraintReferential) {
                    ConstraintReferential cr = (ConstraintReferential) c;
                    if (!cr.isHidden()) {
                        Command command = new ConstraintCommand(cr);
                        if (cr == hotValue) {
                            hotCommand = command;
                        }
                        items.add(command);
                    }
                }
            }

            for (Column c : tf.getTable().getColumns()) {
                if (!c.isHidden()) {
                    Command command = new ColumnCommand(tf.getAlias(), c);
                    if (c == hotValue) {
                        hotCommand = command;
                    }
                    items.add(command);
                }
            }

            ght.setValue(hotCommand);
            ght.setAcceptableValues(items);

            JoinHelper thing = JoinHelper.getParent(tf);
            if (thing != null) {
                tf = thing.getParent();
                hotValue = thing.getConstraint();
            } else {
                tf = null;
            }
        }
    }

    @Override
    public String getSQL(List args) {
        String sql;
        // if (column != null) {
        // sql = column.getSQL();
        // } else {
        sql = Session.quoteIdentifier("" + getColumnName());
        // }
        if (tableAlias != null) {
            sql = Session.quoteIdentifier(tableAlias) + "." + sql;
        }
        // if (schemaName != null) {
        // sql = Parser.quoteIdentifier(schemaName) + "." + sql;
        // }
        return sql;
    }

    @Override
    public void onTable(TableEvent e) {
        // Window.alert("Changed table");
    }

    public TableFilter getTableFilter() {
        // return resolver == null ? null : resolver.getTableFilter();
        for (TableFilter tf2 : getSession().getFilters()) {

            if (StringUtils.equals(tableAlias, tf2.getAlias())) {
                return tf2;
            }
        }
        return null; // XXX or blowup?
    }

//    private class ClearCommand extends Command2 {
//        ClearCommand() {
//            super("Please select...");
//        }
//
//        @Override
//        public void execute() {
//            // TOxDO 00 a Select "PLease select" for column and goes bang. in getSql?
//            // really want this at all? Ever selected? Defaults to first column...
//            // A) Default to first column and get rid of this
//            // B) Don't default and make this work ok
//            updateColumn(null, null);
//        }
//    }

    private class ColumnCommand extends CommandWithLabel {
        private final String alias;
        private final Column column;

        ColumnCommand(String a, Column c) {
            super(c);
            assert (c != null);
            this.alias = a;
            this.column = c;
        }

        @Override
        public void execute() {
            updateColumn(alias, column);
        }
    }

    private class ConstraintCommand extends CommandWithLabel {
        private final ConstraintReferential constraintReferential;

        ConstraintCommand(ConstraintReferential constraintReferential2) {
            super(constraintReferential2);
            this.constraintReferential = constraintReferential2;
        }

        @Override
        public void execute() {
            selectConstraintRef(constraintReferential);
        }
    }
}
