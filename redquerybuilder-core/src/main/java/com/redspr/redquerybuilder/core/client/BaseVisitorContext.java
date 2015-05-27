package com.redspr.redquerybuilder.core.client;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.expression.Comparison;
import com.redspr.redquerybuilder.core.client.expression.ConditionAndOr;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.client.expression.Parameter;


public class BaseVisitorContext implements VisitorContext {

    private HasValue hv;

    private HasMessages hm;

    private String nodeType;

    private String nodeValue;

    public BaseVisitorContext(BaseSqlWidget baseSqlWidget) {
        if (baseSqlWidget instanceof Parameter) {
            Widget w = ((Parameter) baseSqlWidget).getEditorWidget();

            if (w instanceof HasValue) {
                hv = (HasValue) w;
            }
            nodeType = NodeType.PARAMETER;
            nodeValue = "?";
        } else if (baseSqlWidget instanceof Comparison) {
            nodeType = NodeType.COMPARISON;
            nodeValue = ((Comparison) baseSqlWidget).getOperatorName();
        } else if (baseSqlWidget instanceof ConditionAndOr) {
            nodeType = NodeType.LOGIC;
            nodeValue = ((ConditionAndOr) baseSqlWidget).getString();
        } else if (baseSqlWidget instanceof ExpressionColumn) {
            ExpressionColumn ec = (ExpressionColumn) baseSqlWidget;
            nodeType = NodeType.COLUMN;
            nodeValue = ec.getQualifiedColumnName();
        } else if (baseSqlWidget instanceof Select){
            Select select = (Select) baseSqlWidget;
            nodeType = NodeType.SELECT;
            nodeValue = "WIP";
        } else {
            throw new RuntimeException("What is " + baseSqlWidget.getClass());
        }
        if (baseSqlWidget instanceof HasMessages) {
            hm = (HasMessages) baseSqlWidget;
        }
    }

    @Override
    public String getNodeType() {
        return nodeType;
    }

    @Override
    public String getNodeName() {
        return nodeValue;
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
