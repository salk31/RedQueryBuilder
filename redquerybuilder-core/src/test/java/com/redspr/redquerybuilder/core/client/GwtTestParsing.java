package com.redspr.redquerybuilder.core.client;

import java.util.ArrayList;

import org.junit.Test;

import com.redspr.redquerybuilder.core.client.command.Parser;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.Session;

public class GwtTestParsing extends AbstractTest {

    // https://github.com/salk31/RedQueryBuilder/issues/42
    @Test
    public void testWildcard() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        Select s = (Select) p.parseOnly("SELECT * FROM Log l");
        String sql = s.getSQL(new ArrayList());
        assertEquals("SELECT *\nFROM Log L", sql);
    }

    // https://github.com/salk31/RedQueryBuilder/issues/42
    @Test
    public void testAliasedWildcard() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        Select s = (Select) p.parseOnly("SELECT l.* FROM Log l JOIN Person p ON l.parent = p.id");
        String sql = s.getSQL(new ArrayList());
        assertEquals("SELECT L.*\nFROM Log L\nINNER JOIN PERSON P ON L.PARENT = P.ID", sql);
    }
}
