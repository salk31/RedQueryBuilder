package com.redspr.redquerybuilder.core.client;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.expression.Parameter;


public class BaseVisitorContext implements VisitorContext {

    private HasValue hv;

    private HasMessages hm;

    public BaseVisitorContext(BaseSqlWidget baseSqlWidget) {
        if (baseSqlWidget instanceof Parameter) {
            Widget w = ((Parameter) baseSqlWidget).getEditorWidget();

            if (w instanceof HasValue) {
                hv = (HasValue) w;
            }
        }
        if (baseSqlWidget instanceof HasMessages) {
            hm = (HasMessages) baseSqlWidget;
        }
    }

    @Override
    public HasMessages asHasMessages() {
        return hm;
    }

    @Override
    public HasValue asHasValue() {
      return hv;
    }

}
