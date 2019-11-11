package com.powsybl.cgmes.conversion.update;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;

public abstract class AbstractIidmToCgmes {

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    public static <K, V> Map.Entry<String, CgmesPredicateDetails> entry(String key, CgmesPredicateDetails value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, V> Collector<Map.Entry<String, CgmesPredicateDetails>, ?, Map<String, CgmesPredicateDetails>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    protected static final String POWER_TRANSFORMER = "PowerTransformer";
    protected static final String ID_END1 = "idEnd1";
    protected static final String ID_END2 = "idEnd2";
    protected static final String PHASE_TAP_CHANGER = "PhaseTapChanger";
    protected static final String RATIO_TAP_CHANGER = "RatioTapChanger";
    protected static final String TRANSFORMER_END = "TransformerEnd";
    protected final String TRANSFORMER_WINDING = "TransformerWinding";
    protected final String SUBREGION_ID = "subRegionId";
    protected final String SUBREGION_NAME = "subRegionName";
    protected final String REGION_NAME = "regionName";
    protected final String REGION_ID = "regionId";

    protected static String value;
    protected static String newSubject;
}
