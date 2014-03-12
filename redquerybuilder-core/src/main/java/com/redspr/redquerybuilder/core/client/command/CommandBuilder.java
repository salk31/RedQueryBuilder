package com.redspr.redquerybuilder.core.client.command;

import java.sql.SQLException;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget.Callback;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.Session;

/**
 * Container for the command - currently only ever SELECT
 */
public class CommandBuilder extends SimplePanel implements
        HasValueChangeHandlers<Select> {

    private final Select select;

    public Select getSelect() {
        return select;
    }

    public CommandBuilder(Session session2) throws SQLException {
        this(session2, null, null);
    }

    public CommandBuilder(Session session, String sql, List<Object> args)
            throws SQLException {

        session.setCommandBuilder(this);

        if (sql == null || sql.isEmpty()) {
            select = new Select(session);
        } else {
            Parser p = new Parser(session);
            if (args != null) { // XXX unit test for this
                for (Object a : args) {
                    session.getValueRegistry().add(a);
                }
            }
            select = (Select) p.parseOnly(sql);
        }

        setWidget(select);

        fireDirty();
    }

    public void fireDirty() {
        select.traverse(new Callback() {
            @Override
            public void handle(BaseSqlWidget w) {
                w.onDirty();
            }
        });

        ValueChangeEvent.fire(this, select);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Select> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }
}
