package com.redspr.redquerybuilder.core.shared.meta;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

// XXX custom editors in Config?
public class Editor implements HasStyleName, Serializable, IsSerializable {
    public static class TextEditor extends Editor {
        @Override
        public Object getDefault() {
            return "";
        }
    };
    public static class DateEditor extends Editor {
        public static final String FORMAT = "format";
        @Override
        public Object getDefault() {
            return new Date();
        }
    };
    public static class BooleanEditor extends Editor {
        @Override
        public Object getDefault() {
            return Boolean.FALSE;
        }
    };

    public static class SelectEditor extends Editor {

    }

    public static class NumberEditor extends Editor {
    }


    // XXX - rubbish, only used by JSON?
    private static final Map<String, Editor> editorByName = new HashMap<String, Editor>();

    private static Editor valueOf2(String name) {
        if ("STRING".equals(name) || "TEXT".equals(name)) {
            return new TextEditor();
        } else if ("DATE".equals(name)) {
            return new DateEditor();
        } else if ("SUGGEST".equals(name)) {
            return new SuggestEditor();
        } else if ("SELECT".equals(name)) {
            return new SelectEditor();
        } else if ("NUMBER".equals(name)) {
            return new NumberEditor();
        } else {
            throw new RuntimeException("No editor for " + name);
        }
    }

    public static Editor valueOf(String name) {
        Editor e = editorByName.get(name);
        if (e == null) {
            e = valueOf2(name);
            editorByName.put(name, e);
        }
        return e;
    }

    // XXX map mush
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    private String styleName;

    public Object getDefault() {
        return null;
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public String getStyleName() {
        return styleName;
    }

    @Override
    public void setStyleName(String p) {
        this.styleName = p;
    }
}
