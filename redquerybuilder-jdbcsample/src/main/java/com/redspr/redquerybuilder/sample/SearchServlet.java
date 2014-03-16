package com.redspr.redquerybuilder.sample;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class SearchServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:db1",
                    "sa", "");
            conn.createStatement().execute(
                    "RUNSCRIPT FROM 'classpath:/world.sql'");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            String sql = req.getParameter("sql");
            String[] args = req.getParameterValues("arg");

            final int page = Integer.parseInt(req.getParameter("page")) - 1;
            final int rp = Integer.parseInt(req.getParameter("rp"));
            final int r0 = page * rp;
            final int r1 = r0 + rp;

            // TODO 01 read only and really limited
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(
                    "jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;ACCESS_MODE_DATA=r",
                    "sa", "");

            PreparedStatement st = conn.prepareStatement(sql);
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    st.setString(i + 1, args[i]);
                }
            }

            ResultSet rs = st.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int numColumns = rsmd.getColumnCount();
            JSONObject root = new JSONObject();
            JSONArray rows = new JSONArray();
            // JSONObject data = new JSONObject();

            // if ("install".equals(type)) {

            // }
            // }

            // if ("meta".equals(type)) {
            // JSONArray metas = new JSONArray();
            // root.put("meta", metas);
            // //root.put("data", data);
            // //{display: 'ISO', name : 'foo', width : 40, sortable : true,
            // align: 'center'}
            // for (int i = 1; i <= numColumns; i++) {
            // JSONObject meta = new JSONObject();
            // metas.put(meta);
            // meta.put("display", rsmd.getColumnLabel(i));
            // meta.put("name", rsmd.getColumnName(i));
            // meta.put("width", 8 * rsmd.getColumnDisplaySize(i));
            // meta.put("sortable", true);
            // meta.put("align", "left");
            // }
            // } else {
            root.put("rows", rows);
            root.put("page", 1);
            // TODO 00 what if arg is null? At the moment blows up
            // TODO 00 number field start with String

            int count = -1;
            while (rs.next()) {
                count++;
                if (count < r0 || count >= r1) {
                    continue;
                }

                JSONObject cell = new JSONObject();
                rows.put(cell);
                JSONArray obj = new JSONArray();
                cell.put("cell", obj);
                for (int i = 1; i < numColumns + 1; i++) {
                    // TODO 01 nulls!
                    switch (rsmd.getColumnType(i)) {
                    case java.sql.Types.BIGINT:
                        obj.put(rs.getInt(i));
                        break;
                    case java.sql.Types.BOOLEAN:
                        obj.put(rs.getBoolean(i));
                        break;
                    case java.sql.Types.DOUBLE:
                        obj.put(rs.getDouble(i));
                        break;
                    case java.sql.Types.FLOAT:
                        obj.put(rs.getFloat(i));
                        break;
                    case java.sql.Types.INTEGER:
                    case java.sql.Types.SMALLINT:
                    case java.sql.Types.TINYINT:
                        obj.put(rs.getInt(i));
                        break;
                    // case java.sql.Types.NVARCHAR :
                    // obj.put(col, rs.getNString(i));
                    // break;
                    case java.sql.Types.VARCHAR:
                        obj.put(rs.getString(i));
                        break;
                    case java.sql.Types.DATE:
                        obj.put(rs.getDate(i));
                        break;
                    case java.sql.Types.TIMESTAMP:
                        obj.put(rs.getTimestamp(i));
                        break;
                    default:
                        obj.put(rs.getString(i));
                    }
                }

            }
            root.put("page", page + 1);
            root.put("total", count + 1);
            res.setContentType("application/json");
            root.write(res.getWriter());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
