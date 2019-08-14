package com.powsybl.cgmes.update;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.elements.*;

public class IidmToCgmes {

    public IidmToCgmes(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
    }

    public IidmToCgmes(IidmChange change) {
        this.change = change;
        this.cgmes = null;
    }

    public Map<CgmesPredicateDetails, String> convert(String instanceClassOfIidmChange) throws Exception {

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
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public TwoMaps switcher() {
        switch (getIidmInstanceName()) {
            case SUBSTATION_IMPL:
                SubstationToSubstation sb = new SubstationToSubstation(change);
                mapIidmToCgmesPredicates = sb.mapIidmToCgmesPredicates();
                allCgmesDetails = sb.getAllCgmesDetailsOnCreate();
                break;
            case BUSBREAKER_VOLTAGELEVEL:
                VoltageLevelToVoltageLevel vl = new VoltageLevelToVoltageLevel(change, cgmes);
                mapIidmToCgmesPredicates = vl.mapIidmToCgmesPredicates();
                allCgmesDetails = vl.getAllCgmesDetailsOnCreate();
                break;
            case CONFIGUREDBUS_IMPL:
                BusToTopologicalNode btn = new BusToTopologicalNode(change, cgmes);
                mapIidmToCgmesPredicates = btn.mapIidmToCgmesPredicates();
                allCgmesDetails = btn.getAllCgmesDetailsOnCreate();
                break;
            case TWOWINDINGS_TRANSFORMER_IMPL:
                TwoWindingsTransformerToPowerTransformer twpt = new TwoWindingsTransformerToPowerTransformer(change,
                    cgmes);
                mapIidmToCgmesPredicates = twpt.mapIidmToCgmesPredicates();
                allCgmesDetails = twpt.getAllCgmesDetailsOnCreate();
                break;
            case GENERATOR_IMPL:
                GeneratorToSynchronousMachine gsm = new GeneratorToSynchronousMachine(change, cgmes);
                mapIidmToCgmesPredicates = gsm.mapIidmToCgmesPredicates();
                allCgmesDetails = gsm.getAllCgmesDetailsOnCreate();
                break;
            case LOAD_IMPL:
                LoadToEnergyConsumer lec = new LoadToEnergyConsumer(change, cgmes);
                mapIidmToCgmesPredicates = lec.mapIidmToCgmesPredicates();
                allCgmesDetails = lec.getAllCgmesDetailsOnCreate();
                break;
            case LINE_IMPL:
                LineToACLineSegment lac = new LineToACLineSegment(change);
                mapIidmToCgmesPredicates = lac.mapIidmToCgmesPredicates();
                allCgmesDetails = lac.getAllCgmesDetailsOnCreate();
                break;
            case SHUNTCOMPENSATOR_IMPL:
                ShuntCompensatorToShuntCompensator sc = new ShuntCompensatorToShuntCompensator(change);
                mapIidmToCgmesPredicates = sc.mapIidmToCgmesPredicates();
                allCgmesDetails = sc.getAllCgmesDetailsOnCreate();
                break;
            default:
                LOG.info("This element is not convertable to CGMES");
        }
        TwoMaps result = new TwoMaps(mapIidmToCgmesPredicates, allCgmesDetails);
        return result;
    }

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    // these helpers will be used by all elements to create maps
    public static <String, Object> Map.Entry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <String, Object> Collector<Map.Entry<String, Object>, ?, Map<String, Object>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    public IidmChange change;
    public CgmesModel cgmes;

    private Map<String, Object> mapIidmToCgmesPredicates;
    private Map<CgmesPredicateDetails, String> allCgmesDetails;

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";
    public static final String SHUNTCOMPENSATOR_IMPL = "ShuntCompensatorImpl";

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
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
