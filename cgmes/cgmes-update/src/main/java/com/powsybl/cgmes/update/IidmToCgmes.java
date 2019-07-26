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

import com.powsybl.cgmes.update.elements.GeneratorOnCreate;
import com.powsybl.cgmes.update.elements.SubstationOnCreate;
import com.powsybl.cgmes.update.elements.VoltageLevelOnCreate;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.
    public IidmToCgmes(IidmChange change) {
        this.change = change;
    }

    public Map<MapTriplestorePredicateToContext, String> convert() throws Exception {

        // map Identifiable Instance with its method for IIDM - CGMES conversion
        Map<String, Callable<Map<String, Object>>> getConversionMapper = new HashMap<>();
         getConversionMapper.put("GeneratorImpl", () -> generatorToSynchronousMachine());
        // getConversionMapper.put("LoadImpl", () -> loadToEnergyConsumer());
        getConversionMapper.put("SubstationImpl", () -> substationToSubstation());
        getConversionMapper.put("BusBreakerVoltageLevel", () -> voltageLevelToVoltageLevel());

        // Getting IIDM - CGMES conversion mapper:
        iidmInstanceName = getIidmInstanceName();

        iidmToCgmesMapper = getConversionMapper.get(iidmInstanceName).call();
        mapContextAttributesValuesOfIidmChange = new HashMap<>();

        if (change.getAttribute() != null && change.getNewValueString() != null) {
            String cgmesNewValue = change.getNewValueString();
            MapTriplestorePredicateToContext predicateToContext = (MapTriplestorePredicateToContext) iidmToCgmesMapper
                .get(change.getAttribute());

            mapContextAttributesValuesOfIidmChange.put(predicateToContext, cgmesNewValue);

        } else {
            // for onCreate all fields are inside the Identifiable object.
            // We dont know which they are. So we will itearte over the corresponding
            // element mapper, to get all informed fields.
            switch (iidmInstanceName) {
                case "SubstationImpl":
                    SubstationOnCreate sub = new SubstationOnCreate(change);
                    mapContextAttributesValuesOfIidmChange = sub.getIdentifiableAttributes();
                    break;
                case "BusBreakerVoltageLevel":
                    VoltageLevelOnCreate vl = new VoltageLevelOnCreate(change);
                    mapContextAttributesValuesOfIidmChange = vl.getIdentifiableAttributes();
                    break;
                case "GeneratorImpl":
                    GeneratorOnCreate gr = new GeneratorOnCreate(change);
                    mapContextAttributesValuesOfIidmChange = gr.getIdentifiableAttributes();
                    break;
                default:
                    LOG.info("DEFAULT FROM SWITCH IidmToCgmes ");
            }

        }

        return mapContextAttributesValuesOfIidmChange;
    }

    public String getIidmInstanceName() {
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public static Map<String, Object> generatorToSynchronousMachine() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new MapTriplestorePredicateToContext("rdf:type", "_EQ")),
            entry("name", new MapTriplestorePredicateToContext("cim:IdentifiedObject.name", "_EQ")),
            entry("minQ", new MapTriplestorePredicateToContext("cim:SynchronousMachine.minQ", "_EQ")),
            entry("maxQ", new MapTriplestorePredicateToContext("cim:SynchronousMachine.maxQ", "_EQ")),
            entry("qPercent", new MapTriplestorePredicateToContext("cim:SynchronousMachine.qPercent", "_EQ")),
            entry("ratedS", new MapTriplestorePredicateToContext("cim:SynchronousMachine.ratedS", "_EQ")),
            entry("targetP", new MapTriplestorePredicateToContext("cim:GeneratingUnit.nominalP", "_EQ")),
            entry("minP", new MapTriplestorePredicateToContext("cim:GeneratingUnit.minOperatingP", "_EQ")),
            entry("maxP", new MapTriplestorePredicateToContext("cim:GeneratingUnit.maxOperatingP", "_EQ")),
            entry("GeneratingUnit",
                new MapTriplestorePredicateToContext("cim:SynchronousMachine.MemberOf_GeneratingUnit", "_EQ")))
            .collect(entriesToMap()));
    }

//    public static Map<Object, String> loadToEnergyConsumer() {
//        return Collections.unmodifiableMap(Stream.of(
//            entry("name", "name"),
//            entry("p0", "pfixed"),
//            entry("q0", "qfixed"),
//            entry("VoltageLevel", "MemberOf_EquipmentContainer")).collect(entriesToMap()));
//    }

    public static Map<String, Object> substationToSubstation() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new MapTriplestorePredicateToContext("rdf:type", "_EQ")),
            entry("name", new MapTriplestorePredicateToContext("cim:IdentifiedObject.name", "_EQ")),
            entry("subRegionName", new MapTriplestorePredicateToContext("cim:SubGeographicalRegion.Region", "_EQ")),
            entry("country", new MapTriplestorePredicateToContext("cim:Substation.Region", "_EQ")))
            .collect(entriesToMap()));
    }

    public static Map<String, Object> voltageLevelToVoltageLevel() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new MapTriplestorePredicateToContext("rdf:type", "_EQ")),
            entry("name", new MapTriplestorePredicateToContext("cim:IdentifiedObject.name", "_EQ")),
            entry("BaseVoltage", new MapTriplestorePredicateToContext("cim:VoltageLevel.BaseVoltage", "_EQ")),
            entry("highVoltageLimit", new MapTriplestorePredicateToContext("cim:VoltageLevel.highVoltageLimit", "_EQ")),
            entry("lowVoltageLimit", new MapTriplestorePredicateToContext("cim:VoltageLevel.lowVoltageLimit", "_EQ")),
            entry("Substation", new MapTriplestorePredicateToContext("cim:VoltageLevel.MemberOf_Substation", "_EQ")),
            entry("nominalV", new MapTriplestorePredicateToContext("cim:BaseVoltage.nominalVoltage", "_EQ")))
            .collect(entriesToMap()));
    }

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    public static <String, Object> Map.Entry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <String, Object> Collector<Map.Entry<String, Object>, ?, Map<String, Object>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    public IidmChange change;
    private Map<String, Object> iidmToCgmesMapper;
    public String iidmInstanceName;

    public Map<MapTriplestorePredicateToContext, String> mapContextAttributesValuesOfIidmChange;

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
