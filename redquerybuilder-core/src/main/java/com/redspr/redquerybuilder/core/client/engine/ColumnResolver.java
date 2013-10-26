package com.redspr.redquerybuilder.core.client.engine;

import com.redspr.redquerybuilder.core.shared.meta.Column;

public interface ColumnResolver {
    Column resolveColumn(String alias, String columnName);
}
