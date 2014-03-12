package com.redspr.redquerybuilder.core.client;


import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.expression.Expression;

/**
 *
 * Lifecycle: 1) Parser creates nodes in any order, names rather than refs 2)
 * dirty -
 *
 * @author sam
 */
// XXX kill the event bus?
public class BaseSqlWidget extends Composite {


    {
        MyResources.INSTANCE.css().ensureInjected();
    }

    private final Session session;

    protected BaseSqlWidget(Session session2) {
        this.session = session2;
    }

    public BaseSqlWidget remove(Expression e) {
        throw new IllegalArgumentException();
    }

    public void replace(Expression old, Expression x) {
    }

    public final BaseSqlWidget getParentExpression() {
        Widget w = getParent();
        while (w != null) {
            if (w instanceof BaseSqlWidget) {
                break;
            }
            w = w.getParent();
        }
        return (BaseSqlWidget) w;
    }

    public void onDirty() {
    }

    public void fireDirty() {
        getSession().getCommandBuilder().fireDirty();
    }

    public void traverse(Callback callback) {
        callback.handle(this);
    }

    public interface Callback {
        void handle(BaseSqlWidget w);
        //startVisit
        //endVisit
    }

    public int hook() {
        return this.getAbsoluteTop() + this.getOffsetHeight() / 2;
    }

    protected Session getSession() {
        return session;
    }
}
