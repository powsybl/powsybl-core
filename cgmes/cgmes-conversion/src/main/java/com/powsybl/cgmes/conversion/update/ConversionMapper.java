package com.powsybl.cgmes.conversion.update;

import com.google.common.collect.Multimap;

public interface ConversionMapper {

    Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates();

}
