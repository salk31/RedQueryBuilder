package com.redspr.redquerybuilder.core.client.util;

import com.google.gwt.user.client.Command;
import com.redspr.redquerybuilder.core.shared.meta.HasLabel;

/**
 *
 * A Command with a Label.
 *
 */
public abstract class CommandWithLabel implements Command, HasLabel {
    private final String e;

    public CommandWithLabel(HasLabel e2) {
        this(e2.getLabel());
    }

    public CommandWithLabel(String e2) {
        this.e = e2;
        if (e == null) {
            throw new IllegalArgumentException("Label must not be null");
        }
    }

    @Override
    public String getLabel() {
        return e;
    }

    @Override
    public int hashCode() {
        return e.hashCode();
    }

    @Override
    public boolean equals(Object p) {
        if (p instanceof CommandWithLabel) {
            CommandWithLabel p2 = (CommandWithLabel) p;
            return e.equals(p2.e);
        }
        return false;
    }
}
