package com.redspr.redquerybuilder.core.shared.meta;

public class SuggestRequest extends EnumerateRequest {
    private int limit;

    private String query;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int p) {
        this.limit = p;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String p) {
        this.query = p;
    }


}
