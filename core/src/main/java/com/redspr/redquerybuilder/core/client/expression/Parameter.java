package com.redspr.redquerybuilder.core.client.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.engine.DirtyEvent;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.HasValue2;

/**
 * A parameter of a prepared statement.
 */
public class Parameter extends Expression {

    private Object value;

    private Column lastColumn;

    private Object lastEditorKey;

    private final SimplePanel lb = new SimplePanel();

    public Parameter(Session session) {
        super(session);

        initWidget(lb);

        value = session.getValueRegistry().getY().getValue();
    }

    public Parameter(Session session, int index) {
        super(session);

        initWidget(lb);

        value = session.getValueRegistry().getY(index).getValue();
    }

    public Parameter(Session session, Collection<Expression> children) {
        super(session);

        initWidget(lb); // XXX three times now
        List<Object> value2 = new ArrayList<Object>();
        for (Expression child : children) {
            if (child instanceof Null) {
                continue;
            }
            value2.add(((Parameter) child).value);
        }
        value = value2;
    }

    @Override
    public String getSQL(List args) {
        if (value instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            Collection a = (Collection) value;
            if (a.isEmpty()) {
                sb.append("(NULL)");
            } else {
                for (Object o : a) {
                    if (sb.length() == 0) {
                        sb.append("(?");
                    } else {
                        sb.append(", ?");
                    }
                    args.add(o);
                }
                sb.append(')');
            }

// TODO 00 toSql with no values select IN ()
            return sb.toString();
        } else {
            args.add(value);
            return "?";
        }
    }

    @Override
    public void onDirty(DirtyEvent e) {
        Comparison parent = (Comparison) this.getParentExpression();

        ExpressionColumn otherSide = (ExpressionColumn) parent.getOther(this);
        Column col = otherSide.getColumn();

        Object newEditorKey = EditorWidgetFactory.createKey(col,
                parent.getOperator());

        if (lastColumn != col || !newEditorKey.equals(lastEditorKey)) {

            final Widget w = EditorWidgetFactory.create(newEditorKey, getSession(),
                    col);

            lb.setWidget(w);
            final HasValue<Object> hv = (HasValue<Object>) w;
            hv.addValueChangeHandler(new ValueChangeHandler<Object>() {
                @Override
                public void onValueChange(ValueChangeEvent<Object> event) {
                    value = hv.getValue();
                    if (value instanceof HasValue2) {
                        value = ((HasValue2) value).getValue();
                    }
                    fireChangeEvent();
                }
            });

            if (lastEditorKey != null || value == null) {
                value = col.getEditor().getDefault();
            }

            hv.setValue(value, false);

            lastColumn = col;
            lastEditorKey = newEditorKey;

            String style = col.getStyleName();
            if (style != null) {
                w.addStyleName(style);
            }
        }
    }

    public Widget getEditorWidget() {
        return lb.getWidget();
    }
}
