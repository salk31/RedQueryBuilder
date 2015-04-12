package com.redspr.redquerybuilder.core.client.expression;

import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.client.AbstractTest;
import com.redspr.redquerybuilder.core.shared.meta.Cardinality;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Editor;
import com.redspr.redquerybuilder.core.shared.meta.Operator;

@Ignore
public abstract class AbstractEditorTest<T> extends AbstractTest {

    abstract protected Editor getEditor();

    protected Cardinality getCardinality() {
        return Cardinality.ONE;
    }

    abstract protected T getExample1();

    protected T getEmptyValue() {
        return null;
    }

    private Column getColumn() {
        Column c = new Column();
        c.setEditor(getEditor());
        return c;
    }

    private Operator getOperator() {
        Operator o = new Operator("TEST");
        o.setCardinality(getCardinality());
        return o;
    }

    private Widget asWidget() {
        Object key = EditorWidgetFactory.createKey(getColumn(), getOperator());
        return EditorWidgetFactory.create(key, null, getColumn());
    }

    private HasValue<T> asHasValue() {
        return (HasValue<T>) asWidget();
    }

    @Test
    public void testNullInEmptyOut() {
        HasValue<T> hasValue = asHasValue();
        hasValue.setValue(null);
        assertEquals(getEmptyValue(), hasValue.getValue());
    }

    @Test
    public void testValueInAndOut() {
        HasValue<T> hasValue = asHasValue();
        hasValue.setValue(getExample1());
        T a = getExample1();
        if (a instanceof Date) {
            Date x = (Date) a;
            Date y = (Date) hasValue.getValue();
            Window.alert("x.time=" + x.getTime() + " y.time=" + y.getTime());
        }
        assertEquals(getExample1(), hasValue.getValue());
    }
}
