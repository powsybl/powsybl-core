package com.powsybl.cgmes.update;

public class MapTriplestorePredicateToContext {
    
    public MapTriplestorePredicateToContext(String attributeName,String triplestoreContext) {
        this.attributeName = attributeName;
        this.context = triplestoreContext;
    }
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public String getContext() {
        return context;
    }
    
    private String attributeName;
    private String context;
}
