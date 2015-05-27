package com.redspr.redquerybuilder.core.client.expression;


import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.Visitor;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.util.XWidget;


/**
 * An 'and' or 'or' condition as in WHERE ID=1 AND NAME=?
 */
public class ConditionAndOr extends Condition {
    interface MyUiBinder extends UiBinder<Widget, ConditionAndOr> {
    }

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField
    XWidget<Expression> left;

    @UiField
    XWidget<Expression> right;

    @UiField
    ListBox op;



    /**
     * The AND condition type as in ID=1 AND NAME='Hello'.
     */
    public static final int AND = 0;

    /**
     * The OR condition type as in ID=1 OR NAME='Hello'.
     */
    public static final int OR = 1;

    private int andOrType;

    public ConditionAndOr(Session session, int andOrType2, Expression left2, Expression right2) {
        super(session);
        initWidget(uiBinder.createAndBindUi(this));
        this.andOrType = andOrType2;
        setLeft(left2);
        setRight(right2);

        op.addItem("AND");
        op.addItem("OR");

        op.setSelectedIndex(andOrType2);


    }

    public Expression getLeft() {
        return left.getValue();
    }

    public void setLeft(Expression e) {
        left.setValue(e);
    }

    public Expression getRight() {
        return right.getValue();
    }

    public void setRight(Expression e) {
        right.setValue(e);
    }

    public String getString() {
        switch (andOrType) {
        case AND:
            return "AND";
        case OR:
            return "OR";
        default:
            throw new RuntimeException("andOrType=" + andOrType);
        }
    }

    @Override
    public String getSQL(List args) {
        String sql;
        switch (andOrType) {
        case AND:
            sql = getLeft().getSQL(args) + " AND " + getRight().getSQL(args);
            break;
        case OR:
            sql = getLeft().getSQL(args) + " OR " + getRight().getSQL(args);
            break;
        default:
            throw new RuntimeException("andOrType=" + andOrType);
        }
        return "(" + sql + ")";
    }

    @Override
    public BaseSqlWidget remove(Expression e) {
        BaseSqlWidget parent = getParentExpression();
        if (getLeft() == e) {
            parent.replace(this, getRight());
        } else if (getRight() == e) {
            parent.replace(this, getLeft());
        } else {
            throw new IllegalArgumentException();
        }
        return parent;
    }

    @Override
    public void replace(Expression a, Expression b) {
        if (getLeft() == a) {
            setLeft(b);
        } else if (getRight() == a) {
            setRight(b);
        }
    }

    @Override
    public void acceptChildren(Visitor callback) {
        getLeft().traverse(callback);
        getRight().traverse(callback);
    }

    @UiHandler("op")
    void handleChange(ChangeEvent e) {
        andOrType = op.getSelectedIndex();
        fireDirty();
    }

    @Override
    public int hook() {
        return (getLeft().hook() + getRight().hook()) / 2;
    }

    @Override
    public void onDirty() {
        Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
            @Override
            public boolean execute() {
                Element opOuter = op.getElement().getParentElement();
                Element outerDiv = opOuter.getParentElement();
                Element topDiv = outerDiv.getFirstChildElement();

                int middleOffset = (hook() - getAbsoluteTop()) - opOuter.getOffsetHeight() / 2;

                opOuter.getStyle().setTop(middleOffset, Unit.PX);

                int leftHook = getLeft().hook();
                int rightHook = getRight().hook();

                int borderWidth = 10;

                int h = rightHook - leftHook - borderWidth;
                int top = leftHook - getAbsoluteTop() - borderWidth / 2;

                topDiv.getStyle().setHeight(h, Unit.PX);
                topDiv.getStyle().setTop(top, Unit.PX);

                return false;
        }

        }, 30);
    }
}
