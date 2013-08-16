package com.redspr.redquerybuilder.core.client.command;

import java.sql.SQLException;
import java.util.List;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.redspr.redquerybuilder.core.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.DirtyEvent;
import com.redspr.redquerybuilder.core.client.engine.DirtyEventHandler;
import com.redspr.redquerybuilder.core.client.engine.Session;

/**
 * Container for the command - currently only ever SELECT
 */
public class CommandBuilder extends BaseSqlWidget implements
        HasValueChangeHandlers<Select>, DirtyEventHandler {

    private final Select select;

    public Select getSelect() {
        return select;
    }

    public CommandBuilder(Session session2) throws SQLException {
        this(session2, null, null);
    }

    public CommandBuilder(Session session2, String sql, List args)
            throws SQLException {
        super(session2);

        session2.setCommandBuilder(this);

        if (sql == null || sql.isEmpty()) {
            select = new Select(getSession());
        } else {
            Parser p = new Parser(getSession());
            if (args != null) { // XXX unit test for this
                for (Object a : args) {
                    session2.getValueRegistry().add(a);
                }
            }
            select = (Select) p.parseOnly(sql);
        }

        initWidget(select);

        select.traverse(new Callback() {
            @Override
            public void handle(BaseSqlWidget w) {
                w.onDirty(null);
            }
        });
    }

    @Override
    public void onDirty(DirtyEvent e) {
        ValueChangeEvent.fire(this, select);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Select> handler) {
        return addHandler(handler, ValueChangeEvent.getType());

    }
}
