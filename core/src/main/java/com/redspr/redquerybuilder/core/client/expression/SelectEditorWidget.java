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
package com.redspr.redquerybuilder.core.client.expression;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.EnumerateRequest;


public class SelectEditorWidget extends Composite implements HasValue {
    private final String tableName;
    private final String columnName;
    private final String columnType;
    private final HasValue box;

    private final Map<Object, String> keyToTitle = new LinkedHashMap<Object, String>();

    private final Renderer<Object> renderer =  new AbstractRenderer<Object>() {
        @Override
        public String render(Object object) {
            return keyToTitle.get(object);
        }
    };


    // XXX is holding onto column anyway
    SelectEditorWidget(Session session, final Column col, boolean multi) {
        if (multi) {
            box = new ValueMultiListBox<Object>(renderer);
        } else {
            box = new ValueListBox<Object>(renderer);
        }
        tableName = col.getTable().getName();
        columnName = col.getName();
        columnType = col.getType().getName();

        initWidget((Widget) box);

        Object v = col.getEditor().getDefault();
        box.setValue(v);

        EnumerateRequest er = new EnumerateRequest();
        er.setTableName(tableName);
        er.setColumnName(columnName);
        er.setColumnTypeName(columnType);

        // XXX make all async?
        session.getConfig().fireEnumerate(er,
                new AsyncCallback<SuggestOracle.Response>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        throw new RuntimeException(
                                "not AsyncCallback<Response>.onFailure implemented",
                                caught);
                    }

                    @Override
                    public void onSuccess(SuggestOracle.Response result) {
                        Object v = col.getEditor().getDefault();

                        if (box instanceof ValueListBox) {
                            keyToTitle.put(v, "Please select...");
                        }
                        for (Suggestion s : result.getSuggestions()) {
                            keyToTitle.put(s.getReplacementString(), s.getDisplayString());
                        }

                        if (box instanceof HasConstrainedValue) {
                            ((HasConstrainedValue) box).setAcceptableValues(keyToTitle.keySet());
                        } else {
                            ((ValueMultiListBox) box).setAcceptableValues(keyToTitle.keySet());
                        }
                    }
                });
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler handler) {
        return box.addValueChangeHandler(handler);
    }

    @Override
    public Object getValue() {
        return box.getValue();
    }

    @Override
    public void setValue(Object value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Object value, boolean fireEvents) {
        box.setValue(value, fireEvents);
    }
}
