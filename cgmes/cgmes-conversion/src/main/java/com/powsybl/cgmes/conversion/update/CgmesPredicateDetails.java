package com.powsybl.cgmes.conversion.update;

/**
 * The Class CgmesPredicateDetails contains details we need to know to construct
 * a triple: the predicate name, the triplestrore context, if the value is
 * Literal or a node, and the new Subject if we need to create new element (e.g
 * transformer end)
 */
public class CgmesPredicateDetails {

    public CgmesPredicateDetails(String rdfPredicate, String triplestoreContext, boolean valueIsNode,
        String value, String newSubject) {
        this.rdfPredicate = rdfPredicate;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.value = value;
        this.newSubject = newSubject;
    }

    public CgmesPredicateDetails(String rdfPredicate, String triplestoreContext, boolean valueIsNode,
        String value) {
        this.rdfPredicate = rdfPredicate;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.value = value;
        this.newSubject = null;
    }

    public String getRdfPredicate() {
        return rdfPredicate;
    }

    public String getContext() {
        return context;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    public String getValue() {
        return value;
    }

    public String getNewSubject() {
        return newSubject;
    }

    private String rdfPredicate;
    private String context;
    private boolean valueIsNode;
    private String newSubject;
    private String value;
}
