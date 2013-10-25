package com.redspr.redquerybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Ignore;

import com.google.gwt.dom.client.Element;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle.MultiWordSuggestion;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.redspr.redquerybuilder.core.Configuration;
import com.redspr.redquerybuilder.core.client.conf.IdentifierEscaper;
import com.redspr.redquerybuilder.core.client.engine.Session;
import com.redspr.redquerybuilder.core.client.table.TableFilter;
import com.redspr.redquerybuilder.core.shared.meta.Cardinality;
import com.redspr.redquerybuilder.core.shared.meta.Column;
import com.redspr.redquerybuilder.core.shared.meta.ConstraintReferential;
import com.redspr.redquerybuilder.core.shared.meta.Database;
import com.redspr.redquerybuilder.core.shared.meta.Editor;
import com.redspr.redquerybuilder.core.shared.meta.EnumerateRequest;
import com.redspr.redquerybuilder.core.shared.meta.Operator;
import com.redspr.redquerybuilder.core.shared.meta.Schema;
import com.redspr.redquerybuilder.core.shared.meta.SuggestEditor;
import com.redspr.redquerybuilder.core.shared.meta.SuggestRequest;
import com.redspr.redquerybuilder.core.shared.meta.Table;
import com.redspr.redquerybuilder.core.shared.meta.Type;

@Ignore
public class AbstractTest extends GWTTestCase {

    @Override
    public void gwtSetUp() {
        TableFilter.resetAlias();
        RootPanel.get().clear();
    }

    protected void assertEquals(String html, Widget w) {
        assertEquals(html, w.getElement());
    }

    protected void assertEquals(String html, Element elmt) {
        assertEquals(html.replace('\'', '"'), elmt.getInnerHTML());
    }


    protected Session getSession() {
        return createSession(null);
    }

    protected Session createSession(Configuration config) {
        Schema schema = new Schema();
        Database database = new Database(schema);

        Type stringType = new Type("VARCHAR");
        stringType.setOperators(new Operator[] {
                new Operator("="),
                new Operator("IS NULL", Cardinality.ZERO),
                new Operator("LIKE"), new Operator("CUSTOM_OP") });

        // XXX IN done better as hint or style?
        Type refType = new Type("REFS");
        refType.setEditor(new Editor.SelectEditor());
        refType.setOperators(new Operator[]{
                new Operator("IN", Cardinality.MULTI),
                new Operator("NOT IN", Cardinality.MULTI)});

        Type suggestType = new Type("SUGGEST");
        suggestType.setEditor(new SuggestEditor());
        suggestType.setOperators(new Operator[]{new Operator("=")});

        Type singleRefType = new Type("REF");
        singleRefType.setEditor(new Editor.SelectEditor());
        singleRefType.setOperators(new Operator[] {
                new Operator("="),
                new Operator("IS NULL", Cardinality.ZERO),
                new Operator("CUSTOM_OP", Cardinality.ONE)});

        Type dateType = new Type("DATE");
        dateType.setEditor(new Editor.DateEditor());
        Type[] types = new Type[]{stringType, dateType, refType, singleRefType};
        database.setTypes(types);

        Table person = new Table("PERSON"); // XXX want case sensitivity?
        Column personId = new Column("id", stringType);
        person.add(personId);
        person.add(new Column("sex", singleRefType));
        person.add(new Column("owner", stringType));
        person.add(new Column("category", refType));
        person.add(new Column("category2", refType));
        person.add(new Column("county", suggestType));
        schema.add(person);

        {
            Table log = new Table("Log");
            log.add(new Column("id", stringType));
            log.add(new Column("date", dateType));
            Column pc = new Column("parent", stringType);
            ConstraintReferential fk = new ConstraintReferential("parentfk",
                    log);
            fk.setRefTable(person);
            fk.setColumns(new Column[] {pc });
            fk.setRefColumns(new Column[] {personId });
            log.add(fk);
            log.add(pc);

            schema.add(log);
        }

        {
            Table order = new Table("Order");
            order.add(new Column("date", dateType));
            Column pc = new Column("parent", stringType);
            ConstraintReferential fk = new ConstraintReferential("orderparentfk",
                    order);
            fk.setRefTable(person);
            fk.setColumns(new Column[] {pc });
            fk.setRefColumns(new Column[] {personId });
            order.add(fk);
            order.add(pc);

            schema.add(order);
        }

        if (config == null) {
            config = createSimpleConfig();
        }

        config.setDatabase(database);

        Session session = new Session(config);

        session.setIdentifierEscaper(new IdentifierEscaper() {
           @Override
        public String quote(String id) {
               return id;
           }
        });



        return session;
    }

    private Configuration createSimpleConfig() {
        Configuration config = new Configuration() {
            @Override
            public void fireEnumerate(EnumerateRequest request, AsyncCallback<Response> callback) {
                Collection<Suggestion> s = new ArrayList();
                if ("category".equals(request.getColumnName())) {
                    s.add(new MultiWordSuggestion("A", "A"));
                    s.add(new MultiWordSuggestion("B", "B"));
                    s.add(new MultiWordSuggestion("C", "C"));
                } else if ("category2".equals(request.getColumnName())) {
                    s.add(new MultiWordSuggestion("X", "X"));
                    s.add(new MultiWordSuggestion("Y", "Y"));
                    s.add(new MultiWordSuggestion("Z", "Z"));
                } else {
                    s.add(new MultiWordSuggestion("M", "M"));
                    s.add(new MultiWordSuggestion("F", "F"));
                }
                callback.onSuccess(new Response(s));
            }

            @Override
            public void fireSuggest(SuggestRequest request,
                    AsyncCallback<Response> callback) {
                Collection<Suggestion> s = new ArrayList<Suggestion>();

                s.add(new MultiWordSuggestion("Lancashire", "Lancashire"));
                s.add(new MultiWordSuggestion("Bedfordshire", "Bedfordshire"));
                s.add(new MultiWordSuggestion("Oxfordshire", "Oxfordshire"));
                callback.onSuccess(new Response(s));
            }
        };
        return config;
    }

    protected List<Element> find(Element elmt, String n) {
        List<Element> x = new ArrayList();
        find(elmt, n, x);
        return x;
    }


    private void find(Element elmt, String n, List<Element> list) {
        String[] cn = elmt.getClassName().split(" ");
        for (String n2 : cn) {
        if (n2.equals(n)) {
            list.add(elmt);
        }
        }
        Element s = elmt.getNextSiblingElement();
        if (s != null) {
            find(s, n, list);
        }
        Element c = elmt.getFirstChildElement();
        if (c != null) {
            find(c, n, list);
        }

    }

    @Override
    public String getModuleName() {
        return "RedQueryBuilderCore";
    }
}
