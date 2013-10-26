/*******************************************************************************
* Copyright (c) 2010-2013 Redspr Ltd.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Sam Hough - initial API and implementation
*******************************************************************************/
package com.redspr.redquerybuilder.core.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.redspr.redquerybuilder.core.client.command.CommandBuilder;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.engine.TableEvent;
import com.redspr.redquerybuilder.core.client.engine.TableEventHandler;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.shared.meta.Column;

public class RedQueryBuilder implements EntryPoint {

    private void createCommandBuilder(final Configuration config, String sql, List args)
            throws Exception {

        final Session session = new Session(config);

        final CommandBuilder builder = new CommandBuilder(session, sql, args);

        session.getMsgBus().addHandler(TableEvent.TYPE,
                new TableEventHandler() {
                    @Override
                    public void onTable(TableEvent e) {
                        if (session.getFilters().size() > 0) {
                            config.fireOnTableChange(session.getFilters());
                            // XXX need to do distinct?
                            ObjectArray expr = ObjectArray.newInstance();
                            TableFilter tf = session.getFilters().get(0);
                            String alias = tf.getAlias();
                            for (Column col : tf.getTable().getColumns()) {
                                expr.add(new ExpressionColumn(session, null, alias,
                                    col.getName()));
                            }
                            builder.getSelect().setExpressions(expr);
                        } else {
                            builder.getSelect().setExpressions(null);
                        }
                    }
                });

        builder.addValueChangeHandler(new ValueChangeHandler<Select>() {
            @Override
            public void onValueChange(ValueChangeEvent<Select> event) {
                List<Object> args = new ArrayList<Object>();
                String sql = event.getValue().getSQL(args);

                config.fireOnSqlChange(sql, args);
            }
        });

        builderContainer.setWidget(builder);

        config.fireOnTableChange(session.getFilters());
    }

    private final SimplePanel builderContainer = new SimplePanel();

    @Override
    public void onModuleLoad() {

        //publish();
    }
}
