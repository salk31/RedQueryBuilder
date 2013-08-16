package com.redspr.redquerybuilder.core.client.expression;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;

public class SuggestEditorWidget<T> extends Composite implements HasValue<T> {

    class RqbSuggestOracle extends SuggestOracle {
        @Override
        public void requestSuggestions(final Request request,
                final Callback callback) {
            SuggestRequest sr = new SuggestRequest();
            sr.setTableName(tableName);
            sr.setColumnName(columnName);
            sr.setColumnTypeName(columnType);
            sr.setQuery(request.getQuery());
            sr.setLimit(request.getLimit());

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

        suggestBox = new SuggestBox(new RqbSuggestOracle());
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
