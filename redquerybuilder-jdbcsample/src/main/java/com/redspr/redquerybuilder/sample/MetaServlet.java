package com.redspr.redquerybuilder.sample;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MetaServlet extends HttpServlet {

    private final ResourceBundle bundle = PropertyResourceBundle.getBundle("meta");

    class ForeignKey {
        private String name;
        private String label;
        private String reverseLabel;
        private String fkTableName;
        private final List<String> pkColumnNames = new ArrayList<String>();
        private final List<String> fkColumnNames = new ArrayList<String>();

        JSONObject toJson() throws JSONException {
            JSONObject fk = new JSONObject();
            fk.put("name", name);
            fk.put("label", label);
            fk.put("reverseLabel", reverseLabel);
            fk.put("fkTableName", fkTableName);

            fk.put("fkColumnNames", fkColumnNames);
            fk.put("pkColumnNames", pkColumnNames);
            return fk;
        }
    }

    private String getLocal(String key) {
        String x = bundle.getString(key);
        if (x == null) {
            return key;
        }
        return x;
    }

    private JSONObject op(String name, String label) throws JSONException {
        return op(name, label, "ONE");
    }

    private JSONObject op(String name, String label, String card)
            throws JSONException {
        JSONObject op = new JSONObject();
        op.put("name", name);
        op.put("label", label);
        op.put("cardinality", card);
        return op;
    }

    private JSONArray stringOps() throws JSONException {
        JSONArray ops = new JSONArray();
        ops.put(op("=", "is"));
        ops.put(op("<>", "is not"));
        ops.put(op("LIKE", "like"));
        ops.put(op("<", "less than"));
        ops.put(op(">", "greater than"));

        return ops;
    }

    private JSONArray numberOps() throws JSONException {
        JSONArray ops = new JSONArray();
        ops.put(op("=", "is"));
        ops.put(op("<>", "is not"));
        ops.put(op("<", "less than"));
        ops.put(op(">", "greater than"));
        return ops;
    }

    private JSONArray booleanOps() throws JSONException {
        JSONArray ops = new JSONArray();
        ops.put(op("=", "is"));
        return ops;
    }

    private JSONObject type(String name, String editor, JSONArray ops)
            throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("editor", editor);
        obj.put("operators", ops);
        return obj;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            // TODO 02 fk names - mandatory for i18n purposes?
            // TODO 01 config for this
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:db1",
                    "sa", "");

            DatabaseMetaData dmd = conn.getMetaData();
            JSONObject root = new JSONObject();

            {
                JSONArray types = new JSONArray();
                root.put("types", types);

                types.put(type("CHAR", "SUGGEST", stringOps()));
                types.put(type("NUMERIC", "TEXT", numberOps()));
                types.put(type("INTEGER", "TEXT", numberOps()));
                types.put(type("DECIMAL", "TEXT", numberOps()));
                types.put(type("SMALLINT", "TEXT", numberOps()));
                types.put(type("BOOLEAN", "SELECT", booleanOps()));
            }

            {
                JSONArray tables = new JSONArray();
                root.put("tables", tables);
                ResultSet rs = dmd.getTables(null, null, null,
                        new String[] {"TABLE" });

                while (rs.next()) {

                    JSONObject table = new JSONObject();
                    tables.put(table);
                    String tableName = rs.getString("TABLE_NAME");
                    table.put("name", tableName);
                    JSONArray columns = new JSONArray();
                    table.put("columns", columns);

                    ResultSet rsCols = dmd.getColumns(null, null, tableName,
                            null);

                    while (rsCols.next()) {
                        JSONObject meta = new JSONObject();
                        columns.put(meta);
                        String columnName = rsCols.getString("COLUMN_NAME");
                        meta.put("label", columnName);
                        meta.put("name", columnName);
                        meta.put("size", rsCols.getInt("COLUMN_SIZE"));
                        String type = rsCols.getString("TYPE_NAME");
                        if ("ISOFFICIAL".equals(columnName)) {
                            type = "BOOLEAN";
                        }
                        meta.put("type", type);
                    }

                    JSONArray fks = new JSONArray();
                    table.put("fks", fks);

                    {
                        ResultSet rs2 = dmd.getExportedKeys(null, null,
                                tableName);

                        ForeignKey fk = null;
                        List<ForeignKey> fksx = new ArrayList<ForeignKey>();
                        while (rs2.next()) {
                            int keySeq = rs2.getInt("KEY_SEQ");

                            if (keySeq == 1) {
                                fk = new ForeignKey();
                                String name = rs2.getString("FK_NAME");
                                fk.name = name;
                                fksx.add(fk);

                                String key = "fk." + name;
                                fk.label = getLocal(key);
                                fk.reverseLabel = getLocal(key + ".reverse");

                                fk.fkTableName = rs2.getString("FKTABLE_NAME");
                            }

                            fk.fkColumnNames.add(rs2
                                    .getString("FKCOLUMN_NAME"));
                            fk.pkColumnNames.add(rs2
                                    .getString("PKCOLUMN_NAME"));
                        }
                        for (ForeignKey foo2 : fksx) {
                            fks.put(foo2.toJson());
                        }
                    }
                }
            }

            res.setContentType("application/json");

            root.write(res.getWriter());
        } catch (SQLException ex) {
            throw new ServletException(ex);
        } catch (JSONException ex) {
            throw new ServletException(ex);
        } catch (ClassNotFoundException ex) {
            throw new ServletException(ex);
        }
    }
}
