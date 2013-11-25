package com.redspr.redquerybuilder.js.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

interface Resources extends ClientBundle {
    Resources INSTANCE = GWT.create(Resources.class);

    @Source("SampleConfig1.js")
    TextResource synchronous();

    @Source("MinimalDateMeta.json")
    TextResource minimalDateMeta();
}
