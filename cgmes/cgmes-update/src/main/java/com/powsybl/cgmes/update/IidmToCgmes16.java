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
import com.powsybl.cgmes.update.elements16.*;

/**
 * The Class IidmToCgmes is responsible for mapping back identifiers and
 * attribute names, from Iidm to Cgmes.
 */
public class IidmToCgmes16 extends IidmToCgmes {

    public IidmToCgmes16(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    /**
     * Convert. Maps Identifiable Instance with its method for IIDM - CGMES
     * conversion
     *
     * @return the map
     */
    public Map<CgmesPredicateDetails, String> convert() throws Exception {

        Map<String, Callable<Map<String, Object>>> getConversionMapper = new HashMap<>();
        // for onUpdate we only need to map incoming attribute to cgmes predicate:
//        getConversionMapper.put(SUBSTATION_IMPL,
//            () -> SubstationToSubstation.mapIidmToCgmesPredicates());
//        getConversionMapper.put(BUSBREAKER_VOLTAGELEVEL,
//            () -> VoltageLevelToVoltageLevel.mapIidmToCgmesPredicates());
//        getConversionMapper.put(TWOWINDINGS_TRANSFORMER_IMPL,
//            () -> TwoWindingsTransformerToPowerTransformer.mapIidmToCgmesPredicates());
//        getConversionMapper.put(CONFIGUREDBUS_IMPL,
//            () -> BusToTopologicalNode.mapIidmToCgmesPredicates());
//        getConversionMapper.put(GENERATOR_IMPL,
//            () -> GeneratorToSynchronousMachine.mapIidmToCgmesPredicates());
//        getConversionMapper.put(LOAD_IMPL,
//            () -> LoadToEnergyConsumer.mapIidmToCgmesPredicates());
//        getConversionMapper.put(LCCCONVERTER_STATION_IMPL,
//            () -> LccConverterStationToAcdcConverter.mapIidmToCgmesPredicates());
//        getConversionMapper.put(LINE_IMPL,
//            () -> LineToACLineSegment.mapIidmToCgmesPredicates());

        iidmInstanceName = getIidmInstanceName();

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
                    SubstationToSubstation sub = new SubstationToSubstation(change);
                    mapDetailsOfChange = sub.getAllCgmesDetailsOnCreate();
                    break;
                case BUSBREAKER_VOLTAGELEVEL:
                    VoltageLevelToVoltageLevel vl = new VoltageLevelToVoltageLevel(change);
                    mapDetailsOfChange = vl.getAllCgmesDetailsOnCreate();
                    break;
                case CONFIGUREDBUS_IMPL:
                    BusToTopologicalNode bus = new BusToTopologicalNode(change);
                    mapDetailsOfChange = bus.getAllCgmesDetailsOnCreate();
                    break;
                case TWOWINDINGS_TRANSFORMER_IMPL:
                    TwoWindingsTransformerToPowerTransformer twt = new TwoWindingsTransformerToPowerTransformer(change);
                    mapDetailsOfChange = twt.getAllCgmesDetailsOnCreate();
                    break;
                case GENERATOR_IMPL:
                    GeneratorToSynchronousMachine gr = new GeneratorToSynchronousMachine(change);
                    mapDetailsOfChange = gr.getAllCgmesDetailsOnCreate();
                    break;
                case LOAD_IMPL:
                    LoadToEnergyConsumer load = new LoadToEnergyConsumer(change);
                    mapDetailsOfChange = load.getAllCgmesDetailsOnCreate();
                    break;
                case LCCCONVERTER_STATION_IMPL:
                    LccConverterStationToAcdcConverter lcc = new LccConverterStationToAcdcConverter(change);
                    mapDetailsOfChange = lcc.getAllCgmesDetailsOnCreate();
                    break;
                case LINE_IMPL:
                    LineToACLineSegment line = new LineToACLineSegment(change);
                    mapDetailsOfChange = line.getAllCgmesDetailsOnCreate();
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

    private String cimVersion;
    private Map<String, Object> iidmToCgmesMapper;
    public String iidmInstanceName;

    public Map<CgmesPredicateDetails, String> mapDetailsOfChange;

    private static final String SUBSTATION_IMPL = "SubstationImpl";
    private static final String BUSBREAKER_VOLTAGELEVEL = "BusBreakerVoltageLevel";
    private static final String TWOWINDINGS_TRANSFORMER_IMPL = "TwoWindingsTransformerImpl";
    private static final String CONFIGUREDBUS_IMPL = "ConfiguredBusImpl";
    private static final String GENERATOR_IMPL = "GeneratorImpl";
    private static final String LOAD_IMPL = "LoadImpl";
    private static final String LCCCONVERTER_STATION_IMPL = "LccConverterStationImpl";
    private static final String LINE_IMPL = "LineImpl";

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes16.class);
}
