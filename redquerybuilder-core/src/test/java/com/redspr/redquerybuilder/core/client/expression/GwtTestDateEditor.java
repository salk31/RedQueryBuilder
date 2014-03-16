package com.redspr.redquerybuilder.core.client.expression;

import java.util.Date;

import com.redspr.redquerybuilder.core.shared.meta.Editor;

public class GwtTestDateEditor extends AbstractEditorTest<Date> {

    @Override
    protected Editor getEditor() {
        return new Editor.DateEditor();
    }

    @Override
    protected Date getExample1() {
        return new Date(1971, 1, 6);
    }

}
