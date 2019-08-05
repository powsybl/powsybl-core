package com.powsybl.cgmes.update;

/**
 * The Class CgmesPredicateDetails contains details we need to know to construct
 * a triple: the predicate name, the triplestrore context, if the value is
 * Literal or a node, and the new Subject if we need to create new element (e.g
 * transformer end)
 */
public class CgmesPredicateDetails {

    public CgmesPredicateDetails(String attributeName, String triplestoreContext, boolean valueIsNode) {
        this.attributeName = attributeName;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.newSubject = null;
    }

    public CgmesPredicateDetails(String attributeName, String triplestoreContext, boolean valueIsNode,
        String newSubject) {
        this.attributeName = attributeName;
        this.context = triplestoreContext;
        this.valueIsNode = valueIsNode;
        this.newSubject = newSubject;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getContext() {
        return context;
    }

    public boolean valueIsNode() {
        return valueIsNode;
    }

    public String getNewSubject() {
        return newSubject;
    }

    private String attributeName;
    private String context;
    private boolean valueIsNode;
    private String newSubject;
}
