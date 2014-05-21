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

import java.util.Collection;
import java.util.List;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;

public class SuggestEditorWidget<T> extends Composite implements HasValue<T> {

    private static class SuggestionMenuItem extends MenuItem {

        private static final String STYLENAME_DEFAULT = "item";

        private Suggestion suggestion;

        public SuggestionMenuItem(Suggestion suggestion, boolean asHTML, Command command) {
          super(suggestion.getDisplayString(), asHTML, command);
          // Each suggestion should be placed in a single row in the suggestion
          // menu. If the window is resized and the suggestion cannot fit on a
          // single row, it should be clipped (instead of wrapping around and
          // taking up a second row).
          DOM.setStyleAttribute(getElement(), "whiteSpace", "nowrap");
          setStyleName(STYLENAME_DEFAULT);
          setSuggestion(suggestion);
        }

        public Suggestion getSuggestion() {
          return suggestion;
        }

        public void setSuggestion(Suggestion suggestion) {
          this.suggestion = suggestion;
        }
      }

    private static native List<MenuItem> getItems(MenuBar mb) /*-{
        return mb.@com.google.gwt.user.client.ui.MenuBar::getItems()();
    }-*/;

    private static native MenuItem getSelectedItem(MenuBar mb) /*-{
        return mb.@com.google.gwt.user.client.ui.MenuBar::getSelectedItem()();
    }-*/;

    private static native MenuItem showSuggestions(SuggestBox mb, String x) /*-{
        return mb.@com.google.gwt.user.client.ui.SuggestBox::showSuggestions(Ljava/lang/String;)(x);
    }-*/;


    class ScrollDisplay extends DefaultSuggestionDisplay {
        private int page;
        private ScrollPanel scrollPanel ;
        private MenuBar sm;

        private boolean hasMoreSuggestions;
        private SuggestionCallback callback;

        @Override
        protected Widget decorateSuggestionList(Widget suggestionList) {
            scrollPanel = new ScrollPanel();

            sm = (MenuBar) suggestionList;

            scrollPanel.setWidget(suggestionList);

            scrollPanel.setHeight(Window.getClientHeight() / 3 + "px");
            scrollPanel.setWidth("150px");

            scrollPanel.addScrollHandler(new ScrollHandler() {
                @Override
                public void onScroll(ScrollEvent event) {
                    if (hasMoreSuggestions) {
                   if (scrollPanel.getMaximumVerticalScrollPosition() - scrollPanel.getVerticalScrollPosition() < 40) {
                       requestMore();
                   }
                    }
                }
            });



            return scrollPanel;
        }


        @Override
        protected void setMoreSuggestions(boolean hasMoreSuggestions,
                int numMoreSuggestions) {
            this.hasMoreSuggestions = hasMoreSuggestions;
              // Subclasses may optionally implement.
        }


        @Override
        protected  void showSuggestions(SuggestBox suggestBox,
                Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
                SuggestionCallback callback) {
            this.callback = callback;
            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
        }

        private void scrollInToView() {
            MenuItem item = getSelectedItem(sm);

            if (item != null) {
                scrollPanel.ensureVisible(item);
            }
        }

        private void requestMore() {
            RqbSuggestOracle suggestOracle = new RqbSuggestOracle();
            suggestOracle.setPage(++page);
            Request sr = new Request();
            sr.setQuery(suggestBox.getText());

            suggestOracle.requestSuggestions(sr, new SuggestOracle.Callback() {
                @Override
                public void onSuggestionsReady(Request request, Response response) {
                    for (final Suggestion curSuggestion : response.getSuggestions()) {
                        Command cmd = new Command() {
                            @Override
                            public void execute() {
                                callback.onSuggestionSelected(curSuggestion);
                            }};
                        SuggestionMenuItem smi = new SuggestionMenuItem(curSuggestion, false, cmd);
                        sm.addItem(smi);
                    }
                    setMoreSuggestions(response.hasMoreSuggestions(), 0);
                }
            });
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

        protected void block() {
            if (scrollPanel != null) {
                scrollPanel.addStyleName("rqbBusy");
            }
        }

        protected void unblock() {
            if (scrollPanel != null) {
                scrollPanel.removeStyleName("rqbBusy");
            }
        }
    }

    class RqbSuggestOracle extends SuggestOracle {
        private int page;

        public void setPage(int page) {
            this.page = page;
        }

        private SuggestRequest create(Request request) {
            SuggestRequest sr = new SuggestRequest();
            sr.setTableName(tableName);
            sr.setColumnName(columnName);
            sr.setColumnTypeName(columnType);
            sr.setQuery(request.getQuery());
            sr.setLimit(request.getLimit());
            sr.setPage(page);
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
                    scrollDisplay.unblock();
                }

                @Override
                public void onSuccess(Response result) {
                    scrollDisplay.block();
                    callback.onSuggestionsReady(request, result);
                    scrollDisplay.unblock();
                }
            });
        }
    }

    private final String tableName;
    private final String columnName;
    private final String columnType;
    private final Session session;

    private final SuggestBox suggestBox;

    private final ScrollDisplay scrollDisplay = new ScrollDisplay();

    SuggestEditorWidget(Session session2, Column col) {
        this.session = session2;
        tableName = col.getTable().getName();
        columnName = col.getName();
        columnType = col.getType().getName();

        suggestBox = new SuggestBox(new RqbSuggestOracle(), new TextBox(), scrollDisplay);
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
