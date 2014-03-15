package com.redspr.redquerybuilder.core.client.expression;

import com.redspr.redquerybuilder.core.shared.meta.Editor;

public class GwtTestTextEditor extends AbstractEditorTest<String> {

    @Override
    protected Editor getEditor() {
        return new Editor.TextEditor();
    }

    @Override
    protected String getEmptyValue() {
        return "";
    }

    @Override
    protected String getExample1() {
        return "Flomble";
    }

}
