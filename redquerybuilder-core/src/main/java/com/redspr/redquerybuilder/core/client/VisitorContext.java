package com.redspr.redquerybuilder.core.client;

import com.google.gwt.core.client.js.JsType;
import com.google.gwt.user.client.ui.HasValue;

/**
 *
 * EXPERIMENTAL - likely to change
 *
 * @param <T>
 */
// TODO 0.8.0 move SQL generation to Visitor
@JsType
public interface VisitorContext<T> {
    HasMessages asHasMessages();

    HasValue<T> asHasValue();
}
