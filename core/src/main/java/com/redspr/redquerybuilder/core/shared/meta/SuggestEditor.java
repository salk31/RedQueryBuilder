package com.redspr.redquerybuilder.core.shared.meta;

public class SuggestEditor extends Editor {
    private String key;

    public SuggestEditor() {
    }

    public SuggestEditor(String key2) {
        this.key = key2;
    }

    public String getKey() {
        return key;
    }
}
