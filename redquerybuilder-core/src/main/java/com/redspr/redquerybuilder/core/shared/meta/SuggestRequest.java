package com.redspr.redquerybuilder.core.shared.meta;

public class SuggestRequest extends EnumerateRequest {


    private String query;

    private int limit;

    private int page;

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
