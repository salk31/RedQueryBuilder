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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Cardinality;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Editor;
import com.redspr.redquerybuilder.core.shared.meta.Editor.DateEditor;
import com.redspr.redquerybuilder.core.shared.meta.Editor.SelectEditor;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.SuggestEditor;

// register cardinality and editor...
public class EditorWidgetFactory {

    private static final Map<Object, Factory> KEY_TO_FACTORY =
            new HashMap<Object, Factory>();

    interface Factory {
        Widget create(Session session, Column column);
    }

    private static void add(Class editorClass, Cardinality card, Factory factory) {
        Object key = editorClass.getName() + "_" + card.name();
        KEY_TO_FACTORY.put(key, factory);
    }

    static {
        add(Editor.TextEditor.class, Cardinality.ONE, new Factory() {
            @Override
            public Widget create(Session session, Column column) {
                return new TextBox();
            }
        });

        add(SuggestEditor.class, Cardinality.ONE, new Factory() {
            @Override
            public Widget create(Session session, Column column) {
                return new SuggestEditorWidget(session, column);
            }
        });

        add(Editor.DateEditor.class, Cardinality.ONE, new Factory() {
            @Override
            public Widget create(Session session, Column column) {
                String format = (String) column.getEditor().getAttribute(DateEditor.FORMAT);
                return new DateBox(new DatePicker(), null, new DateBox.DefaultFormat(DateTimeFormat.getFormat(format)));
            }
        });

        add(SelectEditor.class, Cardinality.ONE, new Factory() {
            @Override
            public Widget create(Session session, Column column) {
                return new SelectEditorWidget(session, column, false);
            }
        });

        add(SelectEditor.class, Cardinality.MULTI, new Factory() {
            @Override
            public Widget create(Session session, Column column) {
                return new SelectEditorWidget(session, column, true);
            }
        });
    }



    /**
     *
     * @param col
     * @param operator
     * @return an, opaque, key. Only equals and hashCode should be used.
     */
    static Object createKey(Column col, Operator operator) {
        return col.getEditor().getClass().getName() + "_" + operator.getCardinality().name();
    }

    static Widget create(Object key, Session session, Column col) {
        Factory factory = KEY_TO_FACTORY.get(key);
        if (factory == null) {
            throw new RuntimeException("Unknown editor " + col.getEditor());
        }

        return factory.create(session, col);
    }
}
