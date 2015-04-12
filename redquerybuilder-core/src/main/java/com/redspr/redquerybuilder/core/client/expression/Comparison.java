package com.redspr.redquerybuilder.core.client.expression;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.redspr.redquerybuilder.core.client.BaseSqlWidget;
import com.redspr.redquerybuilder.core.client.Visitor;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ListBox2;
import com.redspr.redquerybuilder.core.client.util.XWidget;
import com.redspr.redquerybuilder.core.shared.meta.Cardinality;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.Type;

/**
 * Example comparison expressions are ID=1, NAME=NAME, NAME IS NULL.
 */
public class Comparison extends Condition {

    private String compareType;
    private final XWidget<Expression> xleft = new XWidget<Expression>();
    private final XWidget<Expression> xright = new XWidget<Expression>();

    private final ListBox2<Operator> op = new ListBox2<Operator>();

    private final Button remove = new Button("-");

    public Comparison(Session session2) {
        super(session2);

        // XXX why do this defaulting here? Not just do it in ExpressionColumn?
        TableFilter tf = getSession().getFilters().get(0);
        Column col = null;
        for (Column col2 : tf.getTable().getColumns()) {
            if (!col2.isHidden()) {
                col = col2;
                break;
            }
        }
        if (col == null) {
            throw new RuntimeException("No visible column");
        }

        setLeft(new ExpressionColumn(getSession(), null, tf.getAlias(),
                col.getName()));
        setRight(new Parameter(getSession()));
        compareType = col.getType().getOperators()[0].getName();
        init();
    }

    public Comparison(Session session2, String compareType2,
            final Expression left2, final Expression right2) {
        super(session2);
        setLeft(left2);
        setRight(right2);
        compareType = compareType2;
        init();
    }

    private void init() {
        HorizontalPanel v = new HorizontalPanel();
        v.addStyleName("rqbComparison");
        v.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        v.add(xleft);

        v.add(op);
        op.addValueChangeHandler(new ValueChangeHandler<Operator>() {
            @Override
            public void onValueChange(ValueChangeEvent<Operator> event) {
                setOperator(op.getValue());
                fireDirty();
            }
        });

        v.add(xright);

        v.add(remove);
        remove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                remove();
                fireDirty();
            }
        });

        Button add = new Button("+");

        v.add(add);
        add.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                add();
                fireDirty();
            }
        });

        initWidget(v);
    }

    public void setOperator(Operator p) {
        this.compareType = p.getName();

        Cardinality c = p.getCardinality();
        if (c == Cardinality.ZERO) {
            if (getRight() != null) {
                setRight(null);
            }
        } else {
            if (getRight() == null) {
                setRight(new Parameter(getSession()));
            }
        }
    }

    public String getOperatorName() {
        return compareType;
    }

    public Operator getOperator() {
        ExpressionColumn leftec = ((ExpressionColumn) getLeft());
        Column lc = leftec.getColumn();
        if (lc == null) {
            leftec.getColumn();
        }
        Type lt = lc.getType();
        Operator o = lt.getOperatorByName(compareType);
        if (o == null) {
            o = lt.getOperators()[0];
            compareType = o.getName();
        }
        return o;
    }

    @Override
    public void onDirty() {
        Column col = ((ExpressionColumn) getLeft()).getColumn();

        op.setValue(getOperator());
        op.setAcceptableValues(col.getType().getOperators());
    }

    @Override
    public String getSQL(List<Object> args) {
        Operator op = getOperator();
        switch (op.getCardinality()) {
        case ZERO:
            return "(" + getLeft().getSQL(args) + " " + op.getName() + ")";
        case ONE:
        case MULTI:
            return "(" + getLeft().getSQL(args) + " " + getOperatorName() + " "
                    + getRight().getSQL(args) + ")";
        default:
            throw new RuntimeException("Unknown cardinality "
                    + op.getCardinality());
        }
    }

    public Expression getLeft() {
        return xleft.getValue();
    }

    public void setLeft(Expression left) {
        xleft.setValue(left);
    }

    public Expression getRight() {
        return xright.getValue();
    }

    public void setRight(Expression right) {
        xright.setValue(right);
    }

    // XXX one side ask about the other...
    // XXX expression have getTypeInfo?
    public Expression getOther(Expression me) {
        if (getLeft() == me) {
            return getRight();
        } else if (getRight() == me) {
            return getLeft();
        } else {
            throw new RuntimeException("Unable to find other side");
        }
    }

    public BaseSqlWidget remove() {
        return getParentExpression().remove(this);
    }

    public void add() {
        ConditionAndOr andOr = new ConditionAndOr(getSession(), 0, new Nop(),
                new Nop());
        getParentExpression().replace(this, andOr);
        andOr.setLeft(this);

        andOr.setRight(new Comparison(getSession()));
    }

    @Override
    public void acceptChildren(Visitor callback) {
        if (xleft.getValue() != null) {
            xleft.getValue().traverse(callback);
        }
        if (xright.getValue() != null) {
            xright.getValue().traverse(callback);
        }
    }
}

