package com.powsybl.cgmes.update.elements;

import java.util.Map;

import com.powsybl.cgmes.update.MapTriplestorePredicateToContext;

public interface ConversionOnCreate {

    public Map<MapTriplestorePredicateToContext, String> getIdentifiableAttributes();

}
