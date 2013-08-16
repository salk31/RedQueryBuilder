package com.redspr.redquerybuilder.core;


import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.DirtyEvent;
import com.redspr.redquerybuilder.core.client.engine.DirtyEventHandler;
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
public class BaseSqlWidget extends Composite implements DirtyEventHandler {


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

    private HandlerRegistration reg;

    @Override
    public void onLoad() {
        if (getSession() != null) {
            reg = getSession().getMsgBus().addHandler(DirtyEvent.TYPE, this);
         }
    }

    @Override
    public void onUnload() {
        if (reg != null) {
            reg.removeHandler();
        }
    }

    @Override
    public void onDirty(DirtyEvent e) {
    }

    public void fireDirty() {
        getSession().getMsgBus().fireEvent(new DirtyEvent());
    }

    public void fireChangeEvent() {
        // XXX need sensible implementation
        getSession().fireChangeEvent();
    }

    public void traverse(Callback callback) {
        callback.handle(this);
    }

    public interface Callback {
        void handle(BaseSqlWidget w);
    }

    public int hook() {
        return this.getAbsoluteTop() + this.getOffsetHeight() / 2;
    }

    protected Session getSession() {
        return session;
    }
}
