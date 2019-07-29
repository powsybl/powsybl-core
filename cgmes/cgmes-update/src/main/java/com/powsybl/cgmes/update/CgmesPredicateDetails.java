package com.powsybl.cgmes.update;

public class CgmesPredicateDetails {

    public CgmesPredicateDetails(String attributeName, String triplestoreContext, boolean isObject) {
        this.attributeName = attributeName;
        this.context = triplestoreContext;
        this.valueIsNode = isObject;
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

    private String attributeName;
    private String context;
    private boolean valueIsNode;
}
