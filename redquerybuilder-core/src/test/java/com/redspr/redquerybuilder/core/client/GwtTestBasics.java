package com.redspr.redquerybuilder.core.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.redspr.redquerybuilder.core.client.command.CommandBuilder;
import com.redspr.redquerybuilder.core.client.command.Parser;
import com.redspr.redquerybuilder.core.client.command.Prepared;
import com.redspr.redquerybuilder.core.client.command.dml.Select;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.engine.TableEvent;
import com.redspr.redquerybuilder.core.client.engine.TableEventHandler;
import com.redspr.redquerybuilder.core.client.expression.Comparison;
import com.redspr.redquerybuilder.core.client.expression.ConditionAndOr;
import com.redspr.redquerybuilder.core.client.expression.ExpressionColumn;
import com.redspr.redquerybuilder.core.client.expression.Nop;
import com.redspr.redquerybuilder.core.client.expression.Parameter;
import com.redspr.redquerybuilder.core.client.expression.SelectEditorWidget;
import com.redspr.redquerybuilder.core.client.table.JoinHelper;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.client.util.ObjectArray;
import com.redspr.redquerybuilder.core.shared.meta.ConstraintReferential;
import com.redspr.redquerybuilder.core.shared.meta.Editor;
import com.redspr.redquerybuilder.core.shared.meta.Table;

public class GwtTestBasics extends AbstractTest {
    @Test
    public void testComparison() throws Exception {
        Parser p = new Parser(getSession());
        Comparison c = (Comparison) p.parseExpression("id = ?");
        ExpressionColumn left = (ExpressionColumn) c.getLeft();
        Parameter param = (Parameter) c.getRight();
    }

    @Test
    public void testSimpleSelect() throws Exception {
        Parser p = new Parser(getSession());
        Prepared prep = p.parseOnly("SELECT id FROM Person WHERE sex = ?");

        Select s = (Select) prep;
        assertTrue(s != null);
        ExpressionColumn ec = (ExpressionColumn) s.getExpressions().get(0);
        assertEquals("ID", ec.getSQL(new ArrayList()));

        assertEquals(1, s.getFilters().size());
        TableFilter tf = s.getFilters().get(0);
        assertTrue(tf != null);
        assertEquals("PERSON", tf.getTable().getName());

        Comparison c = (Comparison) s.getCondition();

        assertEquals("SELECT ID\nFROM PERSON\nWHERE (SEX = ?)", s.getSQL(new ArrayList()));
    }

    @Test
    public void testParseLike() throws Exception {
        Parser p = new Parser(getSession());
        Prepared prep = p.parseOnly("SELECT id FROM Person WHERE owner LIKE ?");

        Select s = (Select) prep;

        assertEquals("SELECT ID\nFROM PERSON\nWHERE (OWNER LIKE ?)", s.getSQL(new ArrayList()));
    }

    @Test
    public void testParseCustom() throws Exception {
        Parser p = new Parser(getSession());
        Prepared prep = p.parseOnly("SELECT id FROM Person WHERE sex CUSTOM_OP ?");

        Select s = (Select) prep;

        assertEquals("SELECT ID\nFROM PERSON\nWHERE (SEX CUSTOM_OP ?)", s.getSQL(new ArrayList()));
    }


    @Test
    public void testInitialArgValue() throws Exception {
        CommandBuilder cb = new CommandBuilder(getSession(),
                "SELECT id FROM Person WHERE sex = ?", new ArrayList());

        List args = new ArrayList();
        cb.getSelect().getSQL(args);

        assertEquals(null, args.get(0));
    }

    @Test
    public void testIsNull() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        Prepared prep = p.parseOnly("SELECT id FROM Person x1 WHERE x1.sex IS NULL");

        Select s = (Select) prep;
        Comparison c = (Comparison) s.getCondition();

        // test output
        List outArgs = new ArrayList();
        String outSql = s.getSQL(new ArrayList());
        assertEquals("SELECT ID\nFROM PERSON X1\nWHERE (X1.SEX IS NULL)", outSql);
    }

    @Test
    public void testParseAlias() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        sess.getValueRegistry().add("foo");
        Prepared prep = p.parseOnly("SELECT id FROM Person x1 WHERE x1.sex = ?");

        Select s = (Select) prep;
        assertTrue(s != null);
        ExpressionColumn ec = (ExpressionColumn) s.getExpressions().get(0);

        assertEquals("ID", ec.getSQL(new ArrayList()));

        assertEquals(1, s.getFilters().size());
        TableFilter tf = s.getFilters().get(0);
        assertTrue(tf != null);
        assertEquals("PERSON", tf.getTable().getName());
        assertEquals("X1", tf.getAlias());

        Comparison c = (Comparison) s.getCondition();

        // test output
        List outArgs = new ArrayList();
        String outSql = s.getSQL(outArgs);
        assertEquals("SELECT ID\nFROM PERSON X1\nWHERE (X1.SEX = ?)", outSql);
        assertEquals("foo", outArgs.get(0));

    }

    @Test
    public void testParseAliasWithBrackets() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        sess.getValueRegistry().add("foo");
        Prepared prep = p.parseOnly("SELECT id FROM Person x1 WHERE (x1.sex = ?)");

        Select s = (Select) prep;
        assertTrue(s != null);
        ExpressionColumn ec = (ExpressionColumn) s.getExpressions().get(0);

        assertEquals("ID", ec.getSQL(new ArrayList()));

        assertEquals(1, s.getFilters().size());
        TableFilter tf = s.getFilters().get(0);
        assertTrue(tf != null);
        assertEquals("PERSON", tf.getTable().getName());
        assertEquals("X1", tf.getAlias());

        Comparison c = (Comparison) s.getCondition();

        // test output
        List outArgs = new ArrayList();
        String outSql = s.getSQL(outArgs);
        assertEquals("SELECT ID\nFROM PERSON X1\nWHERE (X1.SEX = ?)", outSql);
        assertEquals("foo", outArgs.get(0));

    }

    @Test
    public void testAndOrRemove() throws Exception {
        Session s = getSession();
        Nop a = new Nop();
        Nop b = new Nop();
        ConditionAndOr toGo = new ConditionAndOr(s, 0, a, b);
        ConditionAndOr root = new ConditionAndOr(s, 0, toGo, new Nop());
        toGo.remove(a);

        assertTrue(root.getLeft() == b);

        assertEquals("(1=1 AND 1=1)", root.getSQL(new ArrayList()));
    }

    @Test
    public void testSetTableAddCondRemoveCond() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);
        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("PERSON"));
        cb.getSelect().getSQL(new ArrayList());

        Comparison c = new Comparison(s);

        cb.getSelect().addCondition(c);
        cb.fireDirty();

        cb.getSelect().remove(c);
        cb.fireDirty();
    }

    @Test
    public void testSetTableAddCondRemoveCond2() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);
        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("Log"));
        cb.getSelect().getSQL(new ArrayList());

        Comparison c = new Comparison(s);

        cb.getSelect().addCondition(c);
        cb.fireDirty();

        cb.getSelect().remove(c);
        cb.fireDirty();
    }

    @Test
    public void testChangeTableWithCondition() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);
        // RootPanel.get().add(cb);
        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("PERSON"));
        cb.fireDirty();

        Comparison c = new Comparison(s);

        cb.getSelect().addCondition(c);
        cb.fireDirty();

        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("Log"));
        cb.fireDirty();
    }

    @Test
    public void testSimpleJoin() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);

        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("Log"));
        s.getMsgBus().fireEvent(new TableEvent()); // XXX encapsulation
        cb.fireDirty();

        Comparison c = new Comparison(s);
        cb.getSelect().addCondition(c);
        cb.fireDirty();

        ExpressionColumn left = (ExpressionColumn) c.getLeft();

        TableFilter log = cb.getSelect().getFilters().get(0);
        assertEquals("Log", log.getTable().getName());
        assertEquals("x0", log.getAlias());
        for (int i = 0; i < 2; i++) {
            left.selectConstraintRef((ConstraintReferential) log.getTable()
                    .getConstraints().get(0));
            left.updateColumn("x1", s.getDatabase().getMainSchema().findTableOrView(
                    "PERSON").getColumn("sex"));

            {
                JoinHelper thing = JoinHelper.getParent(cb.getSelect()
                        .getTableFilter("x1"));
                assertTrue(log.getTable().getConstraints().get(0) == thing
                        .getConstraint());
                assertEquals("x0", thing.getParent().getAlias());
            }

            {
                JoinHelper thing = JoinHelper.getParent(cb.getSelect()
                        .getTableFilter("x0"));
                assertTrue(thing == null);
            }

            assertEquals(
                    "try " + i,
                    "SELECT \nFROM Log x0\nINNER JOIN PERSON x1 ON x0.parent = x1.id\nWHERE (x1.sex = ?)",
                    cb.getSelect().getSQL(new ArrayList()));
        }
    }

    @Test
    public void testSelectAndUnselectJoin() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);

        cb.getSelect().updateTable(
                s.getDatabase().getMainSchema().findTableOrView("Log"));
        cb.fireDirty();

        Comparison c = new Comparison(s);

        cb.getSelect().addCondition(c);
        cb.fireDirty();

        ExpressionColumn left = (ExpressionColumn) c.getLeft();

        TableFilter log = cb.getSelect().getFilters().get(0);
        left.selectConstraintRef((ConstraintReferential) log.getTable()
                .getConstraints().get(0));

        cb.fireDirty();
        left.updateColumn("x1", s.getDatabase().getMainSchema().findTableOrView(
                "PERSON").getColumn("sex"));

        cb.fireDirty();



        assertEquals(
                "SELECT \nFROM Log x0\nINNER JOIN PERSON x1 ON x0.parent = x1.id\nWHERE (x1.sex = ?)",
                cb.getSelect().getSQL(new ArrayList()));

        // change back to simple column
        left.updateColumn("x0", log.getTable().getColumn("date"));
        cb.fireDirty();
    }

    // select Log -> Person -> Log
    @Test
    public void testSelectAndThenReverseJoin() throws Exception {
        Session s = getSession();
        s.getDatabase().getMainSchema().addReverseConstraints();
        CommandBuilder cb = new CommandBuilder(s);

        Table log = s.getDatabase().getMainSchema().findTableOrView("Log");
        ConstraintReferential logToPerson = (ConstraintReferential) log
        .getConstraints().get(0);
        assertEquals("parentfk", logToPerson.getName());
        Table person = s.getDatabase().getMainSchema().findTableOrView("PERSON");
        ConstraintReferential personToLog = (ConstraintReferential) person
        .getConstraints().get(1);
        assertEquals("Rev parentfk", personToLog.getName());

        cb.getSelect().updateTable(log);
        cb.fireDirty();

        Comparison c = new Comparison(s);

        cb.getSelect().addCondition(c);
        cb.fireDirty();

        ExpressionColumn left = (ExpressionColumn) c.getLeft();

        left.selectConstraintRef(logToPerson);
        cb.fireDirty();

        assertEquals(
                "SELECT \nFROM Log x0\nINNER JOIN PERSON x1 ON x0.parent = x1.id\nWHERE (x1.county = ?)",
                cb.getSelect().getSQL(new ArrayList()));

        left.selectConstraintRef(personToLog);

      //  ValueChangeEvent.fire(left, left.getColumn());
cb.getSelect().onDirty(null);  // TODO 20 need this to make unit test work, async issue?
        cb.fireDirty(); // XXX bad encapsulation, causes garbage collection

        assertEquals(
                "SELECT \nFROM Log x0\nWHERE (x0.parent = ?)",
                cb.getSelect().getSQL(new ArrayList()));
    }

    // SELECT x1.id FROM "Contact" x1 JOIN "Org" x0 ON x1."parent" = x0."id" WHERE (x0."owner" = ?)
    @Test
    public void testParseJoin() throws Exception {
        Session sess = getSession();
        Parser p = new Parser(sess);

        sess.getValueRegistry().add("foo");
        Prepared prep = p.parseOnly("SELECT x1.id FROM \"Log\" x1 JOIN \"PERSON\" x0 ON x1.\"parent\" = x0.\"id\" WHERE (x0.\"owner\" = ?)");

        Select s = (Select) prep;

        assertEquals(2, s.getFilters().size());

        TableFilter tf0 = s.getFilters().get(0);
        TableFilter tf1 = s.getFilters().get(1);
        assertTrue(tf0.getJoin() == tf1);
        //        assertTrue(s != null);
//        ExpressionColumn ec = (ExpressionColumn) s.getExpressions().get(0);
//
//        assertEquals("ID", ec.getSQL(new ArrayList()));
//
//        assertEquals(1, s.getFilters().size());
//        TableFilter tf = s.getFilters().get(0);
//        assertTrue(tf != null);
//        assertEquals("PERSON", tf.getTable().getName());
//        assertEquals("X1", tf.getAlias());
//
//        Comparison c = (Comparison) s.getCondition();

        // test output
        List outArgs = new ArrayList();
        String outSql = s.getSQL(outArgs);
        assertEquals("SELECT X1.ID\nFROM Log X1\nINNER JOIN PERSON X0 ON X1.parent = X0.id\nWHERE (X0.owner = ?)", outSql);
        assertEquals("foo", outArgs.get(0));

    }

    @Test
    public void testAddThirdDuplicateJoin() throws Exception {
        Session s = getSession();
        CommandBuilder cb = new CommandBuilder(s);

        Table person = s.getDatabase().getMainSchema().findTableOrView("PERSON");

        s.getDatabase().getMainSchema().addReverseConstraints();
        cb.getSelect().updateTable(person);

        JoinHelper.getOrCreateFor(s, (ConstraintReferential) person.getConstraints().get(0));
        TableFilter b = JoinHelper.getOrCreateFor(s, (ConstraintReferential) person.getConstraints().get(1));
        assertTrue(b.getJoin() == null);
        TableFilter c = JoinHelper.getOrCreateFor(s, (ConstraintReferential) person.getConstraints().get(1));
        assertTrue(b == c);
    }

    @Test
    public void testTableChangeUpdatingExpression() throws Exception {
        final Session s = getSession();
        final CommandBuilder cb = new CommandBuilder(s);

        s.getMsgBus().addHandler(TableEvent.TYPE, new TableEventHandler() {
            @Override
            public void onTable(TableEvent e) {
                ObjectArray expr = ObjectArray.newInstance();
                String alias = s.getFilters().get(0).getAlias();
                expr.add(new ExpressionColumn(s, null, alias, "id"));
                cb.getSelect().setExpressions(expr);
            }
        });

        Table person = s.getDatabase().getMainSchema().findTableOrView("PERSON");

        cb.getSelect().updateTable(person);

        assertEquals(
                "SELECT x0.id\nFROM PERSON x0",
                cb.getSelect().getSQL(new ArrayList()));
    }

    @Test
    public void testTableFilterGarbageCollect() throws Exception {
        Parser p = new Parser(getSession());
        String sql0 = "SELECT x.id FROM Person x"
            + " LEFT JOIN Person x0 ON x0.id=x.id"
            + " LEFT JOIN Log x1 ON x1.parent=x0.id"
            + " LEFT JOIN Log x2 ON x2.parent=x0.id"
            + " LEFT JOIN Log x4 ON x4.parent=x0.id"
            + " LEFT JOIN Person x3 ON x2.parent=x3.id"
            + " WHERE x1.id=?";

        Prepared prep = p.parseOnly(sql0);

        Select s = (Select) prep;

        s.garbageCollectFilters();

        // collect initial non-sense...
        String sql1 = "SELECT X.ID\nFROM PERSON X\n"
            + "LEFT OUTER JOIN PERSON X0 ON X0.ID = X.ID\n"
            + "LEFT OUTER JOIN Log X1 ON X1.PARENT = X0.ID\n"
            + "WHERE (X1.ID = ?)";
        assertEquals(sql1, s.getSQL(new ArrayList()));

        Comparison c = (Comparison) s.getCondition();
        s.remove(c);
        s.garbageCollectFilters();

        String sql2 = "SELECT X.ID\nFROM PERSON X";
        assertEquals(sql2, s.getSQL(new ArrayList()));
    }

    @Test
    public void testParseAndGenerateIn() throws Exception {
        String sql0 = "SELECT x.id FROM Person x"
            + " WHERE x.category IN (?, ?)";
        List<Object> args0 = args("1", "2");
        CommandBuilder cb = new CommandBuilder(getSession(), sql0, args0);

        Select s = cb.getSelect();
        cb.fireDirty(); // XXX encapsulation

        // collect initial non-sense...
        String sql1 = "SELECT X.ID\nFROM PERSON X\n"
            + "WHERE (X.CATEGORY IN (?, ?))";
        List<Object> args1 = new ArrayList<Object>();
        assertEquals(sql1, s.getSQL(args1));
        assertEquals(args0, args1);
    }

    @Test
    public void testParseAndGenerateNotIn() throws Exception {
        String sql0 = "SELECT x.id FROM Person x"
            + " WHERE x.category NOT IN (?, ?)";
        List<Object> args0 = args("1", "2");
        CommandBuilder cb = new CommandBuilder(getSession(), sql0, args0);

        Select s = cb.getSelect();
        cb.fireDirty(); // XXX encapsulation

        // collect initial non-sense...
        String sql1 = "SELECT X.ID\nFROM PERSON X\n"
            + "WHERE (X.CATEGORY NOT IN (?, ?))";
        List<Object> args1 = new ArrayList<Object>();
        assertEquals(sql1, s.getSQL(args1));
        assertEquals(args0, args1);
    }

    @Test
    public void testParseAndGenerateEmptyIn() throws Exception {
        Parser p = new Parser(getSession());
        String sql0 = "SELECT x.id FROM Person x"
            + " WHERE x.category IN (NULL)";

        Prepared prep = p.parseOnly(sql0);

        Select s = (Select) prep;

        // collect initial non-sense...
        String sql1 = "SELECT X.ID\nFROM PERSON X\n"
            + "WHERE (X.CATEGORY IN (NULL))";
        List args = args();
        assertEquals(sql1, s.getSQL(args));
        assertEquals(0, args.size());
    }

    private List<Object> args(Object... args) {
        List<Object> result = new ArrayList<Object>();
        for (Object a : args) {
            result.add(a);
        }
        return result;
    }

    @Test
    public void testMultiToSingle() throws Exception {
        Session sess = getSession();

        String sql0 = "SELECT x.id FROM Person x"
                + " WHERE x.category IN (?, ?)";
        CommandBuilder cb = new CommandBuilder(sess, sql0, args("A", "B"));
        RootPanel.get().add(cb);
        Select s = cb.getSelect();

        cb.fireDirty();

        {
            Comparison comp = (Comparison) s.getCondition();
            Parameter right = (Parameter) comp.getRight();
            Element selectElmt = right.getElement();

            String html = "<select multiple='multiple' class='gwt-ListBox' size='2'>"
                    + "<option value='A'>A</option>"
                    + "<option value='B'>B</option>"
                    + "<option value='C'>C</option>" + "</select>";
            assertEquals(html, selectElmt);

            assertEquals("SELECT X.ID\nFROM PERSON X\nWHERE (X.CATEGORY IN (?, ?))", s.getSQL(args()));


            // change to sex
            ExpressionColumn left2 = (ExpressionColumn) comp.getLeft();
            left2.updateColumn("X", sess.getDatabase().getMainSchema()
                    .findTableOrView("PERSON").getColumn("category2"));
            cb.fireDirty();
        }

        {
            Comparison comp = (Comparison) s.getCondition();
            Parameter right = (Parameter) comp.getRight();
            Element selectElmt = right.getElement();

            String html = "<select multiple='multiple' class='gwt-ListBox' size='2'>"
                    + "<option value='X'>X</option>"
                    + "<option value='Y'>Y</option>"
                    + "<option value='Z'>Z</option>" + "</select>";
            assertEquals(html, selectElmt);

            assertEquals("SELECT X.ID\nFROM PERSON X\nWHERE (X.category2 IN ?)", s.getSQL(args()));

            // change to sex
            ExpressionColumn left2 = (ExpressionColumn) comp.getLeft();
            left2.updateColumn("X", sess.getDatabase().getMainSchema()
                    .findTableOrView("PERSON").getColumn("sex"));
            cb.fireDirty();
        }

        {
            Comparison comp = (Comparison) s.getCondition();
            Parameter right = (Parameter) comp.getRight();

            Element selectElmt = right.getElement();

            String html = "<select class='gwt-ListBox'>"
                    + "<option value='Please select...'>Please select...</option>"
                    + "<option value='M'>M</option>"
                    + "<option value='F'>F</option>"
                    + "</select>";
            assertEquals(html, selectElmt);

            assertEquals("SELECT X.ID\nFROM PERSON X\nWHERE (X.sex = ?)", s.getSQL(args()));
        }

        // check options in ListBox please, male, female
    }

    @Test
    public void testSingleSelectCorrectOptionsWithEditorOnColumn() throws Exception {
        Session sess = getSession();

        sess.getDatabase().getMainSchema()
        .findTableOrView("PERSON").getColumn("owner").setEditor(new Editor.SelectEditor());

        CommandBuilder cb = new CommandBuilder(sess, "SELECT x.id FROM Person x", null);
        RootPanel.get().add(cb);
        Select s = cb.getSelect();

        Comparison c = s.addFirstCondition();
        s.fireDirty();
        ExpressionColumn left2 = (ExpressionColumn) c.getLeft();
        left2.updateColumn("X", sess.getDatabase().getMainSchema()
                .findTableOrView("PERSON").getColumn("owner"));
        c.fireDirty();


        assertEquals("<select class='gwt-ListBox'>"
                + "<option value='Please select...'>Please select...</option>"
                + "<option value='M'>M</option>"
                + "<option value='F'>F</option>"
                + "</select>", c.getRight());
    }

    @Test
    public void testOptionSelectedFromBootstrap() throws Exception {
        Session sess = getSession();

        List<Object> args = new ArrayList<Object>();
        args.add("M");
        CommandBuilder cb = new CommandBuilder(sess, "SELECT x.id FROM Person x WHERE sex = ?", args);
        RootPanel.get().add(cb);
        Select s = cb.getSelect();

        Comparison c = (Comparison) s.getCondition();
        Parameter p = (Parameter) c.getRight();
        SelectEditorWidget sew = (SelectEditorWidget) p.getEditorWidget();
        assertEquals("M", sew.getValue());
    }

    @Test
    public void testAsyncOptionSelectedFromBootstrap() throws Exception {
        CapturingConfiguration config = new CapturingConfiguration();

        Session sess = createSession(config);

        List<Object> args = new ArrayList<Object>();
        args.add("M");
        CommandBuilder cb = new CommandBuilder(sess, "SELECT x.id FROM Person x WHERE sex = ?", args);
        RootPanel.get().add(cb);
        Select s = cb.getSelect();

        // only now give them the enumeration
        Collection<Suggestion> s2 = new ArrayList();
        s2.add(new MultiWordSuggestion("M", "M"));
        s2.add(new MultiWordSuggestion("F", "F"));

        config.getEnumerateCallback().onSuccess(new Response(s2));

        Comparison c = (Comparison) s.getCondition();
        Parameter p = (Parameter) c.getRight();
        SelectEditorWidget sew = (SelectEditorWidget) p.getEditorWidget();
        assertEquals("M", sew.getValue());
    }

    @Test
    public void testDirtyKeepsValue() throws Exception {
        Session sess = getSession();

        String sql0 = "SELECT x.id FROM Person x"
                + " WHERE x.id=? OR x.id=?";
        List<Object> args = new ArrayList<Object>();
        args.add("13");
        args.add("15");
        CommandBuilder cb = new CommandBuilder(sess, sql0, args);
        RootPanel.get().add(cb);
        Select s = cb.getSelect();

       // cb.fireDirty();

        ConditionAndOr andOr = (ConditionAndOr) s.getCondition();
        Comparison comp = (Comparison) andOr.getLeft();
        Parameter right = (Parameter) comp.getRight();
        Element selectElmt = right.getElement();

        String html = "<input type='text' class='gwt-TextBox' value='13'></input>";
        assertEquals(html, selectElmt);
    }

}
