package com.powsybl.cgmes.conversion.update;

import com.google.common.collect.Multimap;

public interface ConversionMapper {

    Multimap<String, CgmesPredicateDetails> mapIidmToCgmesPredicates();

    final String POWER_TRANSFORMER = "PowerTransformer";
    final String ID_END1 = "idEnd1";
    final String ID_END2 = "idEnd2";
    final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    final String RATIO_TAP_CHANGER = "RatioTapChanger";
    final String TRANSFORMER_END = "TransformerEnd";
    final String TRANSFORMER_WINDING = "TransformerWinding";

}
