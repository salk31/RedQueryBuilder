package com.redspr.redquerybuilder.core.client.engine;

import com.google.gwt.event.shared.GwtEvent;

public class TableEvent extends GwtEvent<TableEventHandler> {
    public static final Type<TableEventHandler> TYPE = new Type<TableEventHandler>();

    // XXX only for root table changing?
    public TableEvent() {
    }

    @Override
    public Type<TableEventHandler> getAssociatedType() {
      return TYPE;
    }

    @Override
    protected void dispatch(TableEventHandler handler) {
      handler.onTable(this);
    }
}
