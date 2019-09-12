package com.powsybl.cgmes.update;

import com.google.common.collect.Multimap;

public interface ConversionMapper {

    Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates();

}
