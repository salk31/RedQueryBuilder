package com.redspr.redquerybuilder.core.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.shared.meta.HasLabel;

public class CommandSuggestBox extends SimplePanel implements HasConstrainedValue<Command> {



    static class CommandSuggestion implements Suggestion {
        private final Command command;

        CommandSuggestion(Command command) {
            this.command = command;
        }

        @Override
        public String getDisplayString() {
            return ((HasLabel) command).getLabel();
        }

        @Override
        public String getReplacementString() {
            return getDisplayString();
        }

        public Command getCommand() {
            return command;
        }
    }

    private final BaseSqlWidget widget;

    private Collection<Command> values;

    private String stickyText;


    private final SuggestOracle oracle = new SuggestOracle() {

        @Override
        public void requestDefaultSuggestions(Request request, Callback callback) {
            requestSuggestions(request, callback);
        }

        @Override
        public void requestSuggestions(Request request, Callback callback) {
            Response response = new Response();
            String query = request.getQuery();
            if (query != null) {
                query = query.toLowerCase();
            }
            List<String> headings = new ArrayList<String>();
            List<Suggestion> suggestions = new ArrayList<Suggestion>();
            String lastHeading = null;
            for (final Command cmd : values) {
                CommandSuggestion cs = new CommandSuggestion(cmd);

                String label = cs.getReplacementString();

                if (label.matches("^\\*.*\\*$")) {
                    lastHeading = label.replaceAll("^\\*|\\*$", "");
                } else {
                    if (query == null || label.toLowerCase().startsWith(query)) {
                        suggestions.add(cs);
                        headings.add(lastHeading);
                        lastHeading = null;
                    }
                }
            }
            response.setSuggestions(suggestions);
            callback.onSuggestionsReady(request, response);

            Widget menu = ((LocalSuggestionDisplay) suggestBox
                    .getSuggestionDisplay()).getMenu();
            if (menu != null) {
                NodeList<Element> nl = menu.getElement().getElementsByTagName(
                        "td");

                for (int i = nl.getLength() - 1; i >= 0; i--) {
                    Element e = nl.getItem(i);
                    String label = headings.get(i);

                    if (label != null) {
                        Element tr = e.getParentElement();
                        Element tbody = tr.getParentElement();
                        Element e2 = DOM.createTD();
                        e2.addClassName("tardisHeading");
                        e2.setInnerText(label);
                        tbody.insertBefore(e2, tr);

                    }
                }
            }
        }
    };

    static class LocalSuggestionDisplay extends DefaultSuggestionDisplay {
        public Widget getMenu() {
            return getPopupPanel().getWidget();
        }
    }

    final SuggestBox suggestBox = new SuggestBox(oracle, new TextBox(), new LocalSuggestionDisplay());


    public CommandSuggestBox(BaseSqlWidget p) {
        this.widget = p;

        suggestBox.getValueBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                stickyText = suggestBox.getText();
                suggestBox.setText("");
                suggestBox.showSuggestionList();
            }
        });

        suggestBox.getValueBox().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                Command matchingCommand = getMatchingCommand();
                if (matchingCommand != null && !suggestBox.getText().equals(stickyText)) {
                    matchingCommand.execute();
                    widget.fireDirty(); // XXX
                } else if (stickyText != null) {
                    suggestBox.setText(stickyText);
                }
            }
        });

        suggestBox.addSelectionHandler(new SelectionHandler<Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                CommandSuggestion cs = (CommandSuggestion) event.getSelectedItem();
                if (cs != null) {
                    cs.getCommand().execute();
                    widget.fireDirty();
                    setValue(cs.getCommand());
                }
            }
        });

        setWidget(suggestBox);
    }

    private Command getMatchingCommand() {
        for (Command cmd : values) {
            if (((HasLabel) cmd).getLabel().equals(suggestBox.getText())) {
                return cmd;
            }
        }
        return null;
    }

    @Override
    public Command getValue() {
        throw new RuntimeException("not HasValue<Command>.getValue implemented");
    }

    @Override
    public void setValue(Command value) {
        setValue(value, false);
    }

    @Override
    public void setValue(Command value, boolean fireEvents) {
        String text = "";
        if (value != null) {
            text = ((HasLabel) value).getLabel();
        }
        suggestBox.setText(text);
        stickyText = text;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Command> handler) {
          return addHandler(handler, ValueChangeEvent.getType());
    }


    @Override
    public void setAcceptableValues(final Collection values) {
        this.values = values;
    }
}
