package com.redspr.redquerybuilder.core.client.engine;

import com.google.gwt.event.shared.GwtEvent;

public class DirtyEvent extends GwtEvent<DirtyEventHandler> {
    public static final Type<DirtyEventHandler> TYPE = new Type<DirtyEventHandler>();

    public DirtyEvent() {
    }

    @Override
    public Type<DirtyEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DirtyEventHandler handler) {
        handler.onDirty(this);
    }
}
