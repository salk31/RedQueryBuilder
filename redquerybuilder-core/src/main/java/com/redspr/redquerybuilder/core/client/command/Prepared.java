package com.redspr.redquerybuilder.core.client.command;


import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.engine.Session;

public class Prepared extends BaseSqlWidget {

    public Prepared(Session session) {
        super(session);
       //initWidget(new Label("Class=" + getClass().getName()));
    }
}
