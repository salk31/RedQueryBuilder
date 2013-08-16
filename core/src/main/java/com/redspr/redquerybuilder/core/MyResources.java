package com.redspr.redquerybuilder.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface MyResources extends ClientBundle {
    MyResources INSTANCE =  GWT.create(MyResources.class);

    @Source("core.css")
    CssResource css();
}
