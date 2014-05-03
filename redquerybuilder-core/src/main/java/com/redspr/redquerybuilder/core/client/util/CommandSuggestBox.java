package com.redspr.redquerybuilder.core.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

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
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.DefaultSuggestionDisplay;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.shared.meta.HasLabel;

public class CommandSuggestBox extends SimplePanel implements HasConstrainedValue<Command> {
    private final BaseSqlWidget widget;
    private static final Logger logger = Logger.getLogger(CommandSuggestBox.class.getName());

    private Collection<Command> values;

    private String stickyText;

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


    SuggestOracle oracle = new SuggestOracle() {
     // TODO __ Ignore search string if exact match or just opened?

                 @Override
                 public void requestDefaultSuggestions(Request request, Callback callback) {
                     Response response = new Response();
                     response.setMoreSuggestions(false);
                     List<Suggestion> suggestions = new ArrayList();
                     for (int j = 0; j < 20; j++) {
                     for (final Command cmd : values) {
                         suggestions.add(new CommandSuggestion(cmd));
                     }}
                     response.setSuggestions(suggestions);
                     callback.onSuggestionsReady(request, response);
                 }

                 @Override
                 public void requestSuggestions(Request request, Callback callback) {
                     Response response = new Response();
                     response.setMoreSuggestions(false);
                     response.setMoreSuggestionsCount(100);
                     List<Suggestion> suggestions = new ArrayList();
                     for (final Command cmd : values) {
                         suggestions.add(new CommandSuggestion(cmd));
                     }
                     response.setSuggestions(suggestions);
                     callback.onSuggestionsReady(request, response);
                 }
             };

    final SuggestBox suggestBox = new SuggestBox(oracle, new TextBox(), new DefaultSuggestionDisplay() {
        @Override
        protected Widget decorateSuggestionList(Widget suggestionList) {
            ScrollPanel sp = new ScrollPanel(suggestionList);
            sp.setHeight("150px");
            sp.setWidth("150px");
            return sp;
          }

        private MenuBar findMenuBar(Widget w) {
            while (w instanceof HasOneWidget) {
                w = ((HasOneWidget) w).getWidget();

            }
            if (w instanceof MenuBar) {
                return (MenuBar) w;
            }
            throw new RuntimeException("Couldn't find MenuBar");
        }

        @Override
        protected void showSuggestions(final SuggestBox suggestBox,
                Collection<? extends Suggestion> suggestions,
                boolean isDisplayStringHTML, boolean isAutoSelectEnabled,
                final SuggestionCallback callback) {
            super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
            logger.warning("1");
            MenuBar mb = getMenuBar(getPopupPanel());
            NodeList<Element> nl = sm.getElement().getElementsByTagName("td");
            logger.warning("1"  + nl);
            for (int i = 0; i < nl.getLength(); i++) {

                    Element e = nl.getItem(i);
                    String label = e.getInnerText();
                    logger.warning("Got text '" + label + "'");
                    if (label.matches("^\\*.*\\*$")) {
                        e.addClassName("tardisHeading");
                        e.setInnerText(label.replaceAll("^\\*|\\*$", ""));
                    }
                    // TODO __ no roll over
                    // TODO __ not clickable
                    // TODO __ search

            }
            logger.warning("2");
        }
    });


    public CommandSuggestBox(BaseSqlWidget p) {
        this.widget = p;

        suggestBox.getValueBox().addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                stickyText = suggestBox.getText();
                suggestBox.showSuggestionList();
            }
        });

        suggestBox.getValueBox().addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                if (stickyText != null) {
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
    public void setAcceptableValues(final Collection<Command> values) {
        this.values = values;
    }
}
