package com.powsybl.cgmes.conversion.update;

public class TripleStoreSimpleUpdateReference {
    public TripleStoreSimpleUpdateReference(String predicate, String context, boolean valueIsNode) {
        this.predicate = predicate;
        this.context = context;
        this.valueIsNode = valueIsNode;
    }

    public String predicate() {
        return predicate;
    }

    public String context() {
        return context;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    private final String predicate;
    private final String context;
    private final boolean valueIsNode;
}
