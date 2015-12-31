package com.redspr.redquerybuilder.core.client.command.dml;


import com.redspr.redquerybuilder.core.client.VisitorContext;
import com.redspr.redquerybuilder.core.client.command.Prepared;
import com.redspr.redquerybuilder.core.client.engine.Session;

public abstract class Query extends Prepared {
    public Query(Session session) {
        super(session);
    }

    @Override
    public String getNodeType() {
        return VisitorContext.NodeType.SELECT;
    }

    @Override
    public String getNodeName() {
        return null;
    }
}
