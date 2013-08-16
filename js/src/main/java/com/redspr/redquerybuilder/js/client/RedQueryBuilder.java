package com.redspr.redquerybuilder.js.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.redspr.redquerybuilder.core.Configuration;
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

    private final SimplePanel builderContainer = new SimplePanel();

    private String lastSql;

    private List<Object> lastArgs;



    static void configure(JsConfiguration config, String sql,
            JsArray<JavaScriptObject> args) throws Exception {
        if (config == null) {
            throw new IllegalArgumentException("Config is null.");
        }

        RedQueryBuilder rqb = new RedQueryBuilder();
        List<Object> args2 = null;
        if (args != null) {
            args2 = new ArrayList<Object>();
            for (int i = 0; i < args.length(); i++) {
                args2.add(args.get(i));
            }
        }

        rqb.install(config, sql, args2);
    }


    private native void publish() /*-{
        $wnd.redQueryBuilder =
            @com.redspr.redquerybuilder.js.client.RedQueryBuilder::configure(Lcom/redspr/redquerybuilder/js/client/JsConfiguration;Ljava/lang/String;Lcom/google/gwt/core/client/JsArray;);
    }-*/;

    private native void fireOnChange(JavaScriptObject onChange, String sql, JavaScriptObject args) /*-{
        onChange(sql, args);
    }-*/;

    private void install(JsConfiguration config2, String sql, List<Object> args)
            throws Exception {
        Configuration config = new ConfigurationAdapter(config2);

        RootPanel.get(config2.getTargetId()).add(builderContainer);
        createCommandBuilder(config, sql, args);
    }

    private void createCommandBuilder(final Configuration config2, String sql,
            List<Object> args) throws Exception {
        final Session session = new Session(config2);

        final CommandBuilder builder = new CommandBuilder(session, sql, args);

        session.getMsgBus().addHandler(TableEvent.TYPE,
                new TableEventHandler() {
                    @Override
                    public void onTable(TableEvent e) {
                        ObjectArray expr;
                        if (session.getFilters().size() > 0) {
                            config2.fireOnTableChange(session.getFilters());
                            // XXX need to do distinct?
                            expr = ObjectArray.newInstance();
                            TableFilter tf = session.getFilters().get(0);
                            String alias = tf.getAlias();
                            for (Column col : tf.getTable().getColumns()) {
                                expr.add(new ExpressionColumn(session, null, alias,
                                    col.getName()));
                            }
                        } else {
                            expr = null;
                        }
                        builder.getSelect().setExpressions(expr);
                    }
                });

        builder.addValueChangeHandler(new ValueChangeHandler<Select>() {
            @Override
            public void onValueChange(ValueChangeEvent<Select> event) {
                List<Object> args = new ArrayList<Object>();
                String sql = event.getValue().getSQL(args);

                config2.fireOnSqlChange(sql, args);

                lastSql = sql;
                lastArgs = args;
            }
        });

        builderContainer.setWidget(builder);

        config2.fireOnTableChange(session.getFilters());
    }


    @Override
    public void onModuleLoad() {
        publish();
    }
}
