package com.powsybl.cgmes.conversion.update;

public class TripleStoreChange {

    public TripleStoreChange(String queryName, String subject, TripleStoreChangeParams params) {
        this.queryName = queryName;
        this.subject = subject;
        this.params = params;
    }

    public String queryName() {
        return queryName;
    }

    public String subject() {
        return subject;
    }

    public TripleStoreChangeParams params() {
        return params;
    }

    private final String queryName;
    private final String subject;
    private final TripleStoreChangeParams params;
}
