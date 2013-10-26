package com.redspr.redquerybuilder.core.client.engine;


import com.google.gwt.event.shared.HandlerManager;
import com.redspr.redquerybuilder.core.client.Configuration;
import com.redspr.redquerybuilder.core.client.ValueRegistry;
import com.redspr.redquerybuilder.core.client.command.CommandBuilder;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.conf.IdentifierEscaper;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.Database;
import com.redspr.redquerybuilder.core.shared.meta.Table;

public class Session {

    private static IdentifierEscaper identifierEscaper = new IdentifierEscaper() {
        @Override
        public String quote(String id) {
            return "\"" + id + "\"";
        }
    };

  public void setIdentifierEscaper(IdentifierEscaper p) {
      identifierEscaper = p;
  }

  /**
   * Add double quotes around an identifier if required.
   *
   * @param s the identifier
   * @return the quoted identifier
   */
  public static String quoteIdentifier(String s) {
      return identifierEscaper.quote(s);
  }

    @Deprecated
    private CommandBuilder cb;

    private Select select;

    private Configuration config;

    private final ValueRegistry valueRegistry = new ValueRegistry();

    private final Database database;

    private final HandlerManager msgbus;


  // XXX 00 remove one of these constructors
  public Session(Configuration config2) {
      this(config2.getDatabase());
      this.config = config2;
  }

  @Deprecated
  public Session(Database database2) {
      database = database2;
      msgbus = new HandlerManager(this);
  }

    public Configuration getConfig() {
        return config;
    }

  @Deprecated
  public void setCommandBuilder(CommandBuilder p) {
      cb = p;
  }

  public Database getDatabase() {
      return database;
  }


  public HandlerManager getMsgBus() {
      return msgbus;
  }


  public void setSelect(Select p) {
      select = p;
  }

  public Column resolveColumn(String alias, String columnName) {
      return select.resolveColumn(alias, columnName);
  }

  public ObjectArray<TableFilter> getFilters() {
      return select.getFilters();
  }

  public TableFilter getTableFilter(Table t) {
      for (TableFilter tf : select.getFilters()) {
          if (tf.getTable().equals(t)) {
              return tf;
          }
      }
      return null;
  }

  public TableFilter createTableFilter(Table t) {
      TableFilter tf = new TableFilter(this, t, TableFilter.newAlias(), select);
      select.addTableFilter(tf, true); // XXX really is true?
      return tf;
  }

  public TableFilter getOrCreateTableFilter(Table t) {
      TableFilter tf = getTableFilter(t);
      if (tf == null) {
          tf = createTableFilter(t);
      }
      return tf;
  }

  //public Table getTable(String alias) {
  //    return select.getTable(alias);
  //}

  public Table getRootTable() {
      // XXX maybe if no table then grab default?
      // or should select always have one table?
      // or ui shouldn't offer condition button till table?
      return select.getFilters().get(0).getTable();
  }

    public ValueRegistry getValueRegistry() {
        return valueRegistry;
    }

    public void fireChangeEvent() {
        cb.onDirty(null);
    }


}

