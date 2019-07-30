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

import com.powsybl.cgmes.update.elements.*;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.
    public IidmToCgmes(IidmChange change) {
        this.change = change;
    }

    public Map<CgmesPredicateDetails, String> convert() throws Exception {

        // map Identifiable Instance with its method for IIDM - CGMES conversion
        Map<String, Callable<Map<String, Object>>> getConversionMapper = new HashMap<>();
        getConversionMapper.put("SubstationImpl", () -> substationToSubstation());
        getConversionMapper.put("BusBreakerVoltageLevel", () -> voltageLevelToVoltageLevel());
        getConversionMapper.put("ConfiguredBusImpl", () -> busToTopologicalNode());
        getConversionMapper.put("GeneratorImpl", () -> generatorToSynchronousMachine());
        getConversionMapper.put("LoadImpl", () -> loadToEnergyConsumer());

        // Getting IIDM - CGMES conversion mapper:
        iidmInstanceName = getIidmInstanceName();

        iidmToCgmesMapper = getConversionMapper.get(iidmInstanceName).call();
        mapChangeDetails = new HashMap<>();

        if (change.getAttribute() != null && change.getNewValueString() != null) {
            String cgmesNewValue = change.getNewValueString();
            CgmesPredicateDetails predicateToContext = (CgmesPredicateDetails) iidmToCgmesMapper
                .get(change.getAttribute());

            mapChangeDetails.put(predicateToContext, cgmesNewValue);

        } else {
            // for onCreate all fields are inside the Identifiable object.
            // We dont know which they are. So we will itearte over the corresponding
            // element mapper, to get all informed fields.
            switch (iidmInstanceName) {
                case "SubstationImpl":
                    SubstationOnCreate sub = new SubstationOnCreate(change);
                    mapChangeDetails = sub.getIdentifiableAttributes();
                    break;
                case "ConfiguredBusImpl":
                    BusOnCreate bus = new BusOnCreate(change);
                    mapChangeDetails = bus.getIdentifiableAttributes();
                    break;
                case "BusBreakerVoltageLevel":
                    VoltageLevelOnCreate vl = new VoltageLevelOnCreate(change);
                    mapChangeDetails = vl.getIdentifiableAttributes();
                    break;
                case "GeneratorImpl":
                    GeneratorOnCreate gr = new GeneratorOnCreate(change);
                    mapChangeDetails = gr.getIdentifiableAttributes();
                    break;
                case "LoadImpl":
                    LoadOnCreate load = new LoadOnCreate(change);
                    mapChangeDetails = load.getIdentifiableAttributes();
                    break;
                default:
                    LOG.info("This element is not convertable to CGMES");
            }

        }

        return mapChangeDetails;
    }

    public String getIidmInstanceName() {
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public static Map<String, Object> substationToSubstation() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("subRegionName", new CgmesPredicateDetails("cim:SubGeographicalRegion.Region", "_EQ", true)),
            entry("country", new CgmesPredicateDetails("cim:Substation.Region", "_EQ", true)))
            .collect(entriesToMap()));
    }

    public static Map<String, Object> voltageLevelToVoltageLevel() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("BaseVoltage", new CgmesPredicateDetails("cim:VoltageLevel.BaseVoltage", "_EQ", true)),
            entry("highVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.highVoltageLimit", "_EQ", false)),
            entry("lowVoltageLimit", new CgmesPredicateDetails("cim:VoltageLevel.lowVoltageLimit", "_EQ", false)),
            entry("Substation", new CgmesPredicateDetails("cim:VoltageLevel.MemberOf_Substation", "_EQ", true)),
            entry("nominalV", new CgmesPredicateDetails("cim:BaseVoltage.nominalVoltage", "_EQ", false)))
            .collect(entriesToMap()));
    }

    public static Map<String, Object> busToTopologicalNode() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_TP", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_TP", false)))
            .collect(entriesToMap()));
    }

    public static Map<String, Object> generatorToSynchronousMachine() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("minQ", new CgmesPredicateDetails("cim:SynchronousMachine.minQ", "_EQ", false)),
            entry("maxQ", new CgmesPredicateDetails("cim:SynchronousMachine.maxQ", "_EQ", false)),
            entry("qPercent", new CgmesPredicateDetails("cim:SynchronousMachine.qPercent", "_EQ", false)),
            entry("ratedS", new CgmesPredicateDetails("cim:SynchronousMachine.ratedS", "_EQ", false)),
            entry("targetP", new CgmesPredicateDetails("cim:GeneratingUnit.initialP", "_EQ", false)),
            entry("minP", new CgmesPredicateDetails("cim:GeneratingUnit.minOperatingP", "_EQ", false)),
            entry("maxP", new CgmesPredicateDetails("cim:GeneratingUnit.maxOperatingP", "_EQ", false)),
            entry("GeneratingUnit",
                new CgmesPredicateDetails("cim:SynchronousMachine.MemberOf_GeneratingUnit", "_EQ", true)))
            .collect(entriesToMap()));
    }

    public static Map<String, Object> loadToEnergyConsumer() {
        return Collections.unmodifiableMap(Stream.of(
            entry("rdfType", new CgmesPredicateDetails("rdf:type", "_EQ", false)),
            entry("name", new CgmesPredicateDetails("cim:IdentifiedObject.name", "_EQ", false)),
            entry("p0", new CgmesPredicateDetails("cim:EnergyConsumer.pfixed", "_EQ", false)),
            entry("q0", new CgmesPredicateDetails("cim:EnergyConsumer.qfixed", "_EQ", false)),
            entry("p", new CgmesPredicateDetails("cim:EnergyConsumer.p", "_SSH", false)),
            entry("q", new CgmesPredicateDetails("cim:EnergyConsumer.q", "_SSH", false)),
            entry("VoltageLevel", new CgmesPredicateDetails("cim:Equipment.MemberOf_EquipmentContainer", "_EQ", true)))
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

    public Map<CgmesPredicateDetails, String> mapChangeDetails;

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
