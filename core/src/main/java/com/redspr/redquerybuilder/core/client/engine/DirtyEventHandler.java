package com.redspr.redquerybuilder.core.client.engine;

import com.google.gwt.event.shared.EventHandler;

public interface DirtyEventHandler extends EventHandler {
    void onDirty(DirtyEvent e);
}
