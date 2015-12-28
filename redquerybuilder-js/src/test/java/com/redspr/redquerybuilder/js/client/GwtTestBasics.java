package com.redspr.redquerybuilder.js.client;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.core.client.JsonUtils;

public class GwtTestBasics extends AbstractTest {

// TODO __ test putting dates, numbers into the args


    @Test
    public void testNothing() throws Throwable {
        test(null, "Config is null.");
    }

    @Test
    public void testEmptyConfig() throws Throwable {
        test("{}", "Meta is null.");
    }

    @Test
    public void testNoTables() throws Throwable {
        test("{meta:{}}", "Unable to find table FOO");
    }

    @Test
    public void testNoFksInJson() throws Throwable {
        test("{meta:{tables:[{name:'Foo'}]}}", null);
    }

    @Test
    public void testLegacyFk() {
        JsFk fk = (JsFk) JsonUtils.unsafeEval("{pkColumnNames:['sillyName'], fkTableName:'almostAsSilly', fkColumnNames:['middleSilly']}");
        assertEquals("sillyName", fk.getForeignKeyNames().get(0));
        assertEquals("almostAsSilly", fk.getReferencedTableName());
        assertEquals("middleSilly", fk.getReferencedKeyNames().get(0));
    }



    @Test
    public void testInAndArgs() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, "SELECT priority FROM ticket WHERE priority IN (?)", args("foo"), null);

        builder.fireDirty();

        assertEquals(1, getLastArgs(conf).length());
        assertEquals("foo", getLastArgs(conf).getString(0));
    }

    @Test
    public void testArrayParameterIn() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, "SELECT priority FROM ticket WHERE priority IN (?, ?)", args("foo", "bar"), null);

        builder.fireDirty();

        assertEquals(2, getLastArgs(conf).length());
        assertEquals("foo", getLastArgs(conf).getString(0));
        assertEquals("bar", getLastArgs(conf).getString(1));
    }

    @Test
    public void testDateInAndOut() throws Throwable {
        String json = Resources.INSTANCE.minimalDateMeta().getText();
        JsDate dateIn = JsDate.create();
        dateIn.setUTCFullYear(1914);
        dateIn.setUTCMonth(4);
        dateIn.setUTCDate(15);
        dateIn.setUTCHours(22);
        dateIn.setUTCMinutes(13);
        test(json, "SELECT dob FROM person WHERE dob = ?", args(dateIn), null);

        builder.fireDirty();

        assertEquals(1, getLastArgs(conf).length());
        JsDate dateOut = getLastArgs(conf).getObject(0);
        assertEquals(1914, dateOut.getUTCFullYear());
        assertEquals(4, dateOut.getUTCMonth());
        assertEquals(15, dateOut.getUTCDate());
        assertEquals(22, dateOut.getUTCHours());
        assertEquals(13, dateOut.getUTCMinutes());
    }

    public void testNullSqlAndNullArgs() throws Throwable {
        String json = Resources.INSTANCE.synchronous().getText();

        test(json, null, null, null);

        builder.fireDirty();
    }

    public void testJsList() throws Throwable {
        JsDate dateIn = JsDate.create();
        dateIn.setUTCFullYear(1914);
        dateIn.setUTCMonth(4);
        dateIn.setUTCDate(15);
        dateIn.setUTCHours(22);
        dateIn.setUTCMinutes(13);

        JsArrayMixed mixed = (JsArrayMixed) JsArrayMixed.createArray();
        mixed.push(dateIn);
        mixed.push(123.12d);
        mixed.push("123");
        mixed.push("false");

        List<Object> list = JsList.get().toList(mixed);
        Date dateOut = (Date) list.get(0);
        Date dateOutUtc = new Date(dateOut.getTime() + 60 * 1000 * dateOut.getTimezoneOffset());
        assertEquals(14, dateOutUtc.getYear());
        assertEquals(4, dateOutUtc.getMonth());
        assertEquals(15, dateOutUtc.getDate());
        assertEquals(22, dateOutUtc.getHours());
        assertEquals(13, dateOutUtc.getMinutes());

        assertEquals(new Double(123.12d), list.get(1));
        assertEquals("123", list.get(2));
        assertEquals("false", list.get(3));
    }

}
