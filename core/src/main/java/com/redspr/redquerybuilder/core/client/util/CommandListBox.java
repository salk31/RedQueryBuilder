package com.redspr.redquerybuilder.core.client.util;


import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;

public class CommandListBox extends ListBox2<Command> {
    private final BaseSqlWidget widget;

    public CommandListBox(BaseSqlWidget p) {
        this.widget = p;
        this.addValueChangeHandler(new ValueChangeHandler<Command>() {
            @Override
            public void onValueChange(ValueChangeEvent<Command> event) {
                event.getValue().execute();
                widget.fireDirty();
            }
        });
    }
}
