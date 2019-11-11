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

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";
    public static final String SHUNTCOMPENSATOR_IMPL = "ShuntCompensatorImpl";

    protected static String value;
    protected static String newSubject;
}
