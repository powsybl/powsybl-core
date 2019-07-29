package com.powsybl.cgmes.update.elements;

import java.util.Map;

import com.powsybl.cgmes.update.CgmesPredicateDetails;

public interface ConversionOnCreate {

    public Map<CgmesPredicateDetails, String> getIdentifiableAttributes();

}
