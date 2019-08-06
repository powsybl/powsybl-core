package com.powsybl.cgmes.update;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
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

    private Object element;

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
    public Map<CgmesPredicateDetails, String> convert() throws Exception {

        iidmInstanceName = getIidmInstanceName();

        Map<String, Callable<Map<String, Object>>> getConversionMapper = new HashMap<>();

        Object element = switcher();

        // for onUpdate we only need to map incoming attribute to cgmes predicate:
        getConversionMapper.put(SUBSTATION_IMPL,
            () -> ((SubstationToSubstation) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(BUSBREAKER_VOLTAGELEVEL,
            () -> ((VoltageLevelToVoltageLevel) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(TWOWINDINGS_TRANSFORMER_IMPL,
            () -> ((TwoWindingsTransformerToPowerTransformer) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(CONFIGUREDBUS_IMPL,
            () -> ((BusToTopologicalNode) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(GENERATOR_IMPL,
            () -> ((GeneratorToSynchronousMachine) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(LOAD_IMPL,
            () -> ((LoadToEnergyConsumer) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(LCCCONVERTER_STATION_IMPL,
            () -> ((LccConverterStationToAcdcConverter) element).mapIidmToCgmesPredicatesOnUpdate());
        getConversionMapper.put(LINE_IMPL,
            () -> ((LineToACLineSegment) element).mapIidmToCgmesPredicatesOnUpdate());

        iidmToCgmesMapper = getConversionMapper.get(iidmInstanceName).call();
        mapDetailsOfChange = new HashMap<>();

        if (change.getAttribute() != null && change.getNewValueString() != null) {
            // ths is change onUpdate
            String cgmesNewValue = change.getNewValueString();

            CgmesPredicateDetails mapCgmesPredicateDetails = (CgmesPredicateDetails) iidmToCgmesMapper
                .get(change.getAttribute());

            mapDetailsOfChange.put(mapCgmesPredicateDetails, cgmesNewValue);

        } else {
            // for onCreate all fields are inside the Identifiable object.
            // We dont know which they are. So we will get all informed fields from special
            // special OnCreate classes.
            switch (iidmInstanceName) {
                case SUBSTATION_IMPL:
                    mapDetailsOfChange = ((SubstationToSubstation) element).getAllCgmesDetailsOnCreate();
                    break;
                case BUSBREAKER_VOLTAGELEVEL:
                    mapDetailsOfChange = ((VoltageLevelToVoltageLevel) element).getAllCgmesDetailsOnCreate();
                    break;
                case CONFIGUREDBUS_IMPL:
                    mapDetailsOfChange = ((BusToTopologicalNode) element).getAllCgmesDetailsOnCreate();
                    break;
                case TWOWINDINGS_TRANSFORMER_IMPL:
                    mapDetailsOfChange = ((TwoWindingsTransformerToPowerTransformer) element)
                        .getAllCgmesDetailsOnCreate();
                    break;
                case GENERATOR_IMPL:
                    mapDetailsOfChange = ((GeneratorToSynchronousMachine) element).getAllCgmesDetailsOnCreate();
                    break;
                case LOAD_IMPL:
                    mapDetailsOfChange = ((LoadToEnergyConsumer) element).getAllCgmesDetailsOnCreate();
                    break;
                case LCCCONVERTER_STATION_IMPL:
                    mapDetailsOfChange = ((LccConverterStationToAcdcConverter) element).getAllCgmesDetailsOnCreate();
                    break;
                case LINE_IMPL:
                    mapDetailsOfChange = ((LineToACLineSegment) element).getAllCgmesDetailsOnCreate();
                    break;
                default:
                    LOG.info("This element is not convertable to CGMES");
            }

        }

        return mapDetailsOfChange;
    }

    public String getIidmInstanceName() {
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public Object switcher() {
        switch (iidmInstanceName) {
            case SUBSTATION_IMPL:
                element = new SubstationToSubstation(change);
                break;
            case BUSBREAKER_VOLTAGELEVEL:
                element = new VoltageLevelToVoltageLevel(change, cgmes);
                break;
            case CONFIGUREDBUS_IMPL:
                element = new BusToTopologicalNode(change);
                break;
            case TWOWINDINGS_TRANSFORMER_IMPL:
                element = new TwoWindingsTransformerToPowerTransformer(change);
                break;
            case GENERATOR_IMPL:
                element = new GeneratorToSynchronousMachine(change, cgmes);
                break;
            case LOAD_IMPL:
                element = new LoadToEnergyConsumer(change, cgmes);
                break;
            case LCCCONVERTER_STATION_IMPL:
                element = new LccConverterStationToAcdcConverter(change);
                break;
            case LINE_IMPL:
                element = new LineToACLineSegment(change);
                break;
            default:
                LOG.info("This element is not convertable to CGMES");
        }
        return element;
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
    private String cimVersion;
    private Map<String, Object> iidmToCgmesMapper;
    public String iidmInstanceName;

    public Map<CgmesPredicateDetails, String> mapDetailsOfChange;

    public static final String SUBSTATION_IMPL = "SubstationImpl";
    public static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    public static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    public static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    public static final String GENERATOR_IMPL = "GeneratorImpl";
    public static final String LOAD_IMPL = "LoadImpl";
    public static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    public static final String LINE_IMPL = "LineImpl";

    private VoltageLevelToVoltageLevel vl;

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
