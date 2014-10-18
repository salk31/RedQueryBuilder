package com.redspr.redquerybuilder.core.shared.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class Schema extends DbObjectBase {
    private final Map<String, Table> tables = new LinkedHashMap<String, Table>();

    public Table findTableOrView(String name) {
        return tables.get(name.toUpperCase());
    }

    public Collection<Table> getTables() {
        return tables.values();
    }

    public void add(Table p) {
        tables.put(p.getName().toUpperCase(), p);
    }

    // XXX better place for this?
    // only internal "optimisation"?
    public void addReverseConstraints() {
        List<ConstraintReferential> crs = new ArrayList<ConstraintReferential>();
        for (Table t : getTables()) {
            for (Constraint c : t.getConstraints()) {
                if (c instanceof ConstraintReferential) {
                    crs.add((ConstraintReferential) c);
                }
            }
        }

        for (ConstraintReferential cr : crs) {
            String title = cr.getRevName();
            if (title == null) {
                title = "Rev " + cr.getName();
            }
            ConstraintReferential cr2 = new ConstraintReferential(title, cr.getRefTable());
            cr2.setRefColumns(cr.getColumns());
            cr2.setColumns(cr.getRefColumns());
            cr2.setRefTable(cr.getTable());
            cr.getRefTable().add(cr2);
            cr2.setHidden(cr.isRevHidden());
        }
    }
}
