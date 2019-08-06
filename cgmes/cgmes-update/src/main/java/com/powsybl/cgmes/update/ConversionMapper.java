package com.powsybl.cgmes.update;

import java.util.Map;

public interface ConversionMapper {

    public Map<String, Object> mapIidmToCgmesPredicates();

    public Map<CgmesPredicateDetails, String> getAllCgmesDetailsOnCreate();

}
