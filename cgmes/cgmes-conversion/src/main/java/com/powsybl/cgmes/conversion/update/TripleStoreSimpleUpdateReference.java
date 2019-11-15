package com.powsybl.cgmes.conversion.update;

public class TripleStoreSimpleUpdateReference {
    public TripleStoreSimpleUpdateReference(String predicate, String contextReference, boolean valueIsUri) {
        this.predicate = predicate;
        this.contextReference = contextReference;
        this.valueIsUri = valueIsUri;
    }

    public String predicate() {
        return predicate;
    }

    public String contextReference() {
        return contextReference;
    }

    public boolean valueIsUri() {
        return valueIsUri;
    }

    private final String predicate;
    private final String contextReference;
    private final boolean valueIsUri;
}
