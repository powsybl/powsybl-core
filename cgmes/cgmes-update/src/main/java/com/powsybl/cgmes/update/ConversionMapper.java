package com.powsybl.cgmes.update;

import java.util.Map;

public interface ConversionMapper {

    public Map<String, Object> mapIidmToCgmesPredicatesOnUpdate();

    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate();

}
