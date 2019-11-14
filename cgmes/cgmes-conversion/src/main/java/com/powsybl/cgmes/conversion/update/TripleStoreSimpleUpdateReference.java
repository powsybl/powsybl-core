package com.powsybl.cgmes.conversion.update;

public class TripleStoreSimpleUpdateReference {
    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, boolean valueIsNode) {
        this.predicate = predicate;
        this.contextReference = contextReference;
        this.valueIsNode = valueIsNode;
    }

    public String predicate() {
        return predicate;
    }

    public String contextReference() {
        return contextReference;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    private final String predicate;
    private final String contextReference;
    private final boolean valueIsNode;
}
