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

import java.util.List;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;

public class SuggestEditorWidget<T> extends Composite implements HasValue<T> {

    private static native List<MenuItem> foo(MenuBar mb) /*-{
    return mb.@com.google.gwt.user.client.ui.MenuBar::getItems()();
}-*/;

    private static native MenuItem bar(MenuBar mb) /*-{
    return mb.@com.google.gwt.user.client.ui.MenuBar::getSelectedItem()();
}-*/;

    class ScrollDisplay extends DefaultSuggestionDisplay {
        ScrollPanel scrollPanel ;
        MenuBar sm;
        @Override
        protected Widget decorateSuggestionList(Widget suggestionList) {
            scrollPanel = new ScrollPanel();
            Window.alert("A " + suggestionList.getClass());
            sm = (MenuBar) suggestionList;
            Window.alert("B " + sm);
            scrollPanel.setWidget(suggestionList);
            Window.alert("C ");
            scrollPanel.setHeight(Window.getClientHeight() / 3 + "px");
            scrollPanel.setWidth("150px");
            return scrollPanel;
        }

        private void scrollInToView() {
            MenuItem item = bar(sm);
            scrollPanel.ensureVisible(item);
        }

        @Override
        protected void moveSelectionDown() {
            super.moveSelectionDown();
            scrollInToView();
        }

        @Override
        protected void moveSelectionUp() {
            super.moveSelectionUp();
            scrollInToView();
        }
    }

    class RqbSuggestOracle extends SuggestOracle {
        private SuggestRequest create(Request request) {
            SuggestRequest sr = new SuggestRequest();
            sr.setTableName(tableName);
            sr.setColumnName(columnName);
            sr.setColumnTypeName(columnType);
            sr.setQuery(request.getQuery());
            sr.setLimit(request.getLimit());
            return sr;
        }

        @Override
        public void requestDefaultSuggestions(final Request request,
                final Callback callback) {
            SuggestRequest sr = create(request);

            session.getConfig().fireDefaultSuggest(sr, new AsyncCallback<Response>() {
                @Override
                public void onFailure(Throwable caught) {
                    // XXX log?
                }

                @Override
                public void onSuccess(Response result) {
                    callback.onSuggestionsReady(request, result);
                }
            });
        }

        @Override
        public void requestSuggestions(final Request request,
                final Callback callback) {
            SuggestRequest sr = create(request);

            session.getConfig().fireSuggest(sr, new AsyncCallback<Response>() {
                @Override
                public void onFailure(Throwable caught) {
                    // XXX log?
                }

                @Override
                public void onSuccess(Response result) {
                    callback.onSuggestionsReady(request, result);
                }
            });
        }
    }

    private final String tableName;
    private final String columnName;
    private final String columnType;
    private final Session session;

    private final SuggestBox suggestBox;

    SuggestEditorWidget(Session session2, Column col) {
        this.session = session2;
        tableName = col.getTable().getName();
        columnName = col.getName();
        columnType = col.getType().getName();

        suggestBox = new SuggestBox(new RqbSuggestOracle(), new TextBox(), new ScrollDisplay());
        suggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                    ValueChangeEvent.fire(SuggestEditorWidget.this, (T) event.getSelectedItem()
                            .getReplacementString());

            }
        });
        suggestBox.addValueChangeHandler(new ValueChangeHandler() {

            @Override
            public void onValueChange(ValueChangeEvent event) {
                ValueChangeEvent.fire(SuggestEditorWidget.this, (T) event.getValue());
            }

        });

        suggestBox.getValueBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                suggestBox.showSuggestionList();
            }
        });
        //suggestBox.setLimit(1000);

        initWidget(suggestBox);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<T> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public T getValue() {
        return (T) suggestBox.getValue();
    }

    @Override
    public void setValue(T p) {
        setValue(p, false);
    }

    @Override
    public void setValue(T p, boolean fireEvents) {
        suggestBox.setValue((String) p, fireEvents);
    }
}
