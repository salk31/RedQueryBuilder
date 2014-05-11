package com.redspr.redquerybuilder.core.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.shared.meta.Database;
import com.redspr.redquerybuilder.core.shared.meta.EnumerateRequest;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;

/**
 * Extend this class to integrate with RedQueryBuilder.
 */
public class Configuration {
    private Database database = new Database();

    private final From from = new From();

    public void fireEnumerate(EnumerateRequest request, AsyncCallback<Response> callback) {
    }

    // XXX not called by CommandBuilder. GWT users meant to use RedQueryBuilder?
    public void fireOnSqlChange(String sql, List<Object> args) {
    }

    public void fireOnTableChange(ObjectArray<TableFilter> filters) {
    }

    public void fireDefaultSuggest(SuggestRequest request, AsyncCallback<Response> callback) {
    }

    public void fireSuggest(SuggestRequest request, AsyncCallback<Response> callback) {
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database p) {
        this.database = p;
    }

    public From getFrom() {
        return from;
    }
}
