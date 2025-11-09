package org.example;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class FilterInvoice {
    QueryInvoicesDAO dao;
    Database db;

    // We want to stub the dao to avoid interacting with database, however it is hard to do so, since dao is initialized internally
    // we need some way to inject dependency which is a stub, so we don't interact with database explicitly
    // we want it to depend on concretion, but only an abstraction.
    // Default constructor (production) — delegates to the other one
    public FilterInvoice() {
        this(new Database());  // must be the first line
    }

    // Dependency-injected constructor (for tests)
    public FilterInvoice(Database db) {
        this.dao = new QueryInvoicesDAO(db);
    }

    public List<Invoice> lowValueInvoices() {
            List<Invoice> all = dao.all();

            return all.stream()
                    .filter(invoice -> invoice.getValue() < 100)
                    .collect(toList());
    }
}
