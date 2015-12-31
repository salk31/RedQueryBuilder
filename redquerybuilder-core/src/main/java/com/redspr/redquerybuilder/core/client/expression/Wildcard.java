package com.redspr.redquerybuilder.core.client.expression;

import java.util.List;

import com.redspr.redquerybuilder.core.client.VisitorContext;
import com.redspr.redquerybuilder.core.client.engine.Session;

public class Wildcard extends Expression {

    private final String table;

    public Wildcard(Session session, String schema, String table) {
        super(session);
        this.table = table;
    }

    @Override
    public String getSQL(List<Object> args) {
        if (table == null) {
            return "*";
        }
        return getSession().quoteIdentifier(table) + ".*";
    }

    @Override
    public String getNodeType() {
        return VisitorContext.NodeType.WILDCARD;
    }

    @Override
    public String getNodeName() {
        return getSQL(null);
    }
}
