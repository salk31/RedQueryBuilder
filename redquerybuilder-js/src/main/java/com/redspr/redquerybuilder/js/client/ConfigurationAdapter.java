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
package com.redspr.redquerybuilder.js.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.redspr.redquerybuilder.core.client.Configuration;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.JsStringArray;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.shared.meta.Cardinality;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.ConstraintReferential;
import com.redspr.redquerybuilder.core.shared.meta.Database;
import com.redspr.redquerybuilder.core.shared.meta.Editor;
import com.redspr.redquerybuilder.core.shared.meta.EnumerateRequest;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;
import com.redspr.redquerybuilder.core.shared.meta.Table;
import com.redspr.redquerybuilder.core.shared.meta.Type;
import com.redspr.redquerybuilder.js.client.conf.JsFrom;

public class ConfigurationAdapter extends Configuration {
    private static class EnumerateCallback implements JsCallback {
        private final AsyncCallback<Response> callback;

        EnumerateCallback(AsyncCallback<Response> callback2) {
            this.callback = callback2;
        }

        @Override
        public void response(JavaScriptObject raw2) {
            JsArray<JsSuggestion> raw = (JsArray<JsSuggestion>) raw2;
            List<Suggestion> suggestions = new ArrayList<Suggestion>();

            for (int i = 0; i < raw.length(); i++) {
                JsSuggestion jss = raw.get(i);
                Suggestion s = new MultiWordSuggestion(jss.getValue(), jss.getLabel());
                suggestions.add(s);
            }

            Response response = new Response(suggestions);
            callback.onSuccess(response);
        }

    }

    private final JsConfiguration config;

    ConfigurationAdapter(JsConfiguration config2) {
        this.config = config2;
        processEditors(config.getEditors());
        addSchema(getDatabase(), config.getMeta());

        processFrom(config.getFrom());
    }

    private void processFrom(JsFrom from) {
        if (from != null) {
            this.getFrom().setVisible(from.isVisible());
        }
    }

    private void processEditors(JsArray<JsEditor> editors) {
        if (editors != null) {
            for (int i = 0; i < editors.length(); i++) {
                JsEditor jsEditor = editors.get(i);
                Editor e = Editor.valueOf(jsEditor.getName());
                if (e == null) {
                    throw new IllegalArgumentException(
                            "Unable to find editor name = '" + jsEditor.getName()
                                    + "'");
                }
                if (jsEditor.getFormat() != null) {
                    e.setAttribute("format", jsEditor.getFormat());
                }
                e.setStyleName(jsEditor.getStyleName());
            }
        }
    }

    private void addSchema(Database database, JsDatabase meta) {
        if (meta == null) {
            throw new IllegalArgumentException("Meta is null.");
        }
        Type[] types = new Type[meta.getTypes().length()];
        for (int i = 0; i < types.length; i++) {
            JsType jsType = meta.getTypes().get(i);
            Type type = new Type(jsType.getName());
            type.setEditor(Editor.valueOf(jsType.getEditor()));
            Operator[] ops = new Operator[jsType.getOperators().length()];
            for (int j = 0; j < ops.length; j++) {
                JsOperator jsOp = jsType.getOperators().get(j);
                Operator op = new Operator(jsOp.getName());
                op.setLabel(jsOp.getLabel());
                String cardName = jsOp.getCardinality();
                if (cardName != null) {
                    op.setCardinality(Cardinality.valueOf(cardName));
                }
                ops[j] = op;
            }
            type.setOperators(ops);
            types[i] = type;
            type.setStyleName(jsType.getStyleName());
        }

        database.setTypes(types);

        for (int i = 0; i < meta.getTables().length(); i++) {
            JsTable jst = meta.getTables().get(i);
            Table t = new Table(jst.getName());
            t.setLabel(jst.getLabel());
            for (int j = 0; j < jst.getColumns().length(); j++) {
                JsColumn jsColumn = jst.getColumns().get(j);
                Column c = new Column(jsColumn.getName(), database.getTypeByName(jsColumn
                        .getType()));
                c.setLabel(jsColumn.getLabel());
                if (jsColumn.getEditor() != null) {
                    c.setEditor(Editor.valueOf(jsColumn.getEditor()));
                }
                c.setStyleName(jsColumn.getStyleName());
                t.add(c);
            }
            database.getMainSchema().add(t);
        }

        for (int i = 0; i < meta.getTables().length(); i++) {
            JsTable jst = meta.getTables().get(i);

            Table pkTable = database.getMainSchema().findTableOrView(
                    jst.getName());
            JsArray<JsFk> fks = jst.getFks();
            for (int j = 0; j < fks.length(); j++) {
                JsFk jsfk = fks.get(j);
                Table fkTable = database.getMainSchema().findTableOrView(
                        jsfk.getReferencedTableName());

                ConstraintReferential fk = new ConstraintReferential(
                        jsfk.getName(), pkTable);
                fk.setLabel(jsfk.getLabel());

                fk.setRefTable(fkTable);
                fk.setColumns(createColumns(pkTable, jsfk.getForeignKeyNames()));
                fk.setRefColumns(createColumns(fkTable, jsfk.getReferencedKeyNames()));
                pkTable.add(fk);

                ConstraintReferential fkr = new ConstraintReferential(
                        jsfk.getName() + ".reverse", fkTable);
                fkr.setLabel(jsfk.getReverseLabel());

                fkr.setRefTable(pkTable);
                fkr.setColumns(createColumns(fkTable, jsfk.getReferencedKeyNames())); // XXX
                                                                       // already
                                                                       // done
                                                                       // this
                fkr.setRefColumns(createColumns(pkTable, jsfk.getForeignKeyNames())); // XXX
                                                                          // already
                                                                          // done
                                                                          // this
                fkTable.add(fkr);
            }
        }
    }

    private Column[] createColumns(Table t, JsStringArray colNames) {
        Column[] result = new Column[colNames.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = t.getColumn(colNames.get(i));
        }
        return result;
    }

    @Override
    public void fireOnSqlChange(String sql, List<Object> args) {
        config.fireOnSqlChange(sql, JsList.get().toJso(args));
    }

    @Override
    public final void fireOnTableChange(ObjectArray<TableFilter> filters) {
        config.fireOnTableChange(filters);
    }

    @Override
    public void fireSuggest(SuggestRequest sr,
            final AsyncCallback<Response> callback) {
        config.fireSuggest(sr.getTableName(), sr.getColumnName(), sr.getColumnTypeName(),
                sr.getQuery(), sr.getLimit(), new EnumerateCallback(callback));
    }

    @Override
    public void fireEnumerate(EnumerateRequest er,
            final AsyncCallback<Response> callback) {
        config.fireEnumerate(er.getTableName(), er.getColumnName(), er.getColumnTypeName(),
                new EnumerateCallback(callback));
    }
}
