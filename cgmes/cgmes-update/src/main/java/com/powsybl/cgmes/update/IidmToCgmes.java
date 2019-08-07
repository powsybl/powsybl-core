package com.powsybl.cgmes.update;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.elements.*;

/**
 * The Class IidmToCgmes is responsible for mapping back identifiers and
 * attribute names, from Iidm to Cgmes.
 */
public class IidmToCgmes {

    public IidmToCgmes(IidmChange change, CgmesModel cgmes) {
        this.change = change;
        this.cgmes = cgmes;
    }

    public IidmToCgmes(IidmChange change) {
        this.change = change;
        this.cgmes = null;
    }

    /**
     * Convert. Maps Identifiable Instance with its method for IIDM - CGMES
     * conversion
     *
     * @return the map
     */
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
                BusToTopologicalNode btn = new BusToTopologicalNode(change);
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
            case LCCCONVERTER_STATION_IMPL:
                LccConverterStationToAcdcConverter lcc = new LccConverterStationToAcdcConverter(change);
                mapIidmToCgmesPredicates = lcc.mapIidmToCgmesPredicates();
                allCgmesDetails = lcc.getAllCgmesDetailsOnCreate();
                break;
            case LINE_IMPL:
                LineToACLineSegment lac = new LineToACLineSegment(change);
                mapIidmToCgmesPredicates = lac.mapIidmToCgmesPredicates();
                allCgmesDetails = lac.getAllCgmesDetailsOnCreate();
                break;
            case PHASETAPCHANGER_IMPL:
                PhaseTapChangerToPhaseTapChanger ptch = new PhaseTapChangerToPhaseTapChanger(change);
                mapIidmToCgmesPredicates = ptch.mapIidmToCgmesPredicates();
                allCgmesDetails = ptch.getAllCgmesDetailsOnCreate();
                break;
            case RATIOTAPCHANGER_IMPL:
                RatioTapChangerToRatioTapChanger rtch = new RatioTapChangerToRatioTapChanger(change);
                mapIidmToCgmesPredicates = rtch.mapIidmToCgmesPredicates();
                allCgmesDetails = rtch.getAllCgmesDetailsOnCreate();
                break;
            // RatioTapChangerStepImpl
            default:
                LOG.info("This element is not convertable to CGMES");
        }
        TwoMaps result = new TwoMaps(mapIidmToCgmesPredicates, allCgmesDetails);
        return result;
    }

    // TODO elena move to its own element.
    public static Map<String, Object> terminalToTerminal() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)))
            .collect(entriesToMap()));
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
    public static final String PHASETAPCHANGER_IMPL = "PhaseTapChangerImpl";
    public static final String RATIOTAPCHANGER_IMPL = "RatioTapChangerImpl";

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
