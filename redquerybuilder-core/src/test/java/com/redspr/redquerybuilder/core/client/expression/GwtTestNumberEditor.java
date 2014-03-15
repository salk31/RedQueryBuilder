package com.redspr.redquerybuilder.core.client.expression;

import com.redspr.redquerybuilder.core.shared.meta.Editor;

public class GwtTestNumberEditor extends AbstractEditorTest<Double> {

    @Override
    protected Editor getEditor() {
        return new Editor.NumberEditor();
    }

    @Override
    protected Double getExample1() {
        return new Double(3.15);
    }

}
