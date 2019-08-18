package com.powsybl.cgmes.update;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.powsybl.cgmes.model.CgmesModel;

public abstract class AbstractIidmToCgmes {
    public AbstractIidmToCgmes(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
    }

    public AbstractIidmToCgmes(IidmChange change) {
        this.change = change;
        this.cgmes = null;
    }

    protected Map<CgmesPredicateDetails, String> convert(String instanceClassOfIidmChange) throws Exception {

        if (instanceClassOfIidmChange.equals("IidmChangeOnUpdate")) {

            mapIidmToCgmesPredicates = switcher().getMapIidmToCgmesPredicates();
            String cgmesNewValue = change.getNewValueString();

            CgmesPredicateDetails mapCgmesPredicateDetails = (CgmesPredicateDetails) mapIidmToCgmesPredicates
                .get(change.getAttribute());

            allCgmesDetails = new HashMap<>();
            allCgmesDetails.put(mapCgmesPredicateDetails, cgmesNewValue);

        } else if (instanceClassOfIidmChange.equals("IidmChangeOnCreate")) {
            // for onCreate all fields are inside the Identifiable object.
            allCgmesDetails = switcher().getAllCgmesDetails();
        } else {
            // here onRemove will go
        }

        return allCgmesDetails;
    }

    public String getIidmInstanceName() {
        return change.getIdentifiable().getClass().getSimpleName();
    }

    protected abstract TwoMaps switcher();

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    // these helpers will be used by all elements to create maps
    public static <String, Object> Map.Entry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <String, Object> Collector<Map.Entry<String, Object>, ?, Map<String, Object>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    protected IidmChange change;
    protected CgmesModel cgmes;
    protected Map<String, Object> mapIidmToCgmesPredicates;
    protected Map<CgmesPredicateDetails, String> allCgmesDetails;

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";
    public static final String SHUNTCOMPENSATOR_IMPL = "ShuntCompensatorImpl";

}

class TwoMaps {
    TwoMaps(Map<String, Object> mapIidmToCgmesPredicates,
        Map<CgmesPredicateDetails, String> allCgmesDetails) {
        this.mapIidmToCgmesPredicates = mapIidmToCgmesPredicates;
        this.allCgmesDetails = allCgmesDetails;
    }

    Map<String, Object> getMapIidmToCgmesPredicates() {
        return mapIidmToCgmesPredicates;
    }

    Map<CgmesPredicateDetails, String> getAllCgmesDetails() {
        return allCgmesDetails;
    }

    private Map<String, Object> mapIidmToCgmesPredicates;
    private Map<CgmesPredicateDetails, String> allCgmesDetails;
}
