package com.powsybl.cgmes.update;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.NamingStrategy;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.

    public IidmToCgmes(IidmChange change) {
        this.change = change;
        this.namingStrategy = new NamingStrategy.Identity();
    }

    public Map<String, String> convert(IidmChange change) throws Exception {

        cgmesChanges = new HashMap<String, String>();
        String cgmesPredicate;
        String cgmesNewValue;
        String cgmesContext;
        String rdfType = null;

        // map Identifiable Instance with its method for IIDM - CGMES conversion
        Map<String, Callable<Map<String, Object>>> getConversionMapper = new HashMap<>();
        // getConversionMapper.put("GeneratorImpl", () ->
        // generatorToSynchronousMachine());
        // getConversionMapper.put("LoadImpl", () -> loadToEnergyConsumer());
        getConversionMapper.put("SubstationImpl", () -> substationToSubstation());
        // getConversionMapper.put("BusBreakerVoltageLevel", () ->
        // voltageLevelToVoltageLevel());

        // Getting IIDM - CGMES conversion mapper:
        iidmInstanceName = getIidmInstanceName();
        iidmToCgmesMapper = getConversionMapper.get(iidmInstanceName).call();

        cgmesSubject = namingStrategy.getCgmesId(change.getIdentifiableId());

        if (change.getAttribute() != null) {
            MapTriplestoreContext mapTriplestoreContext = (MapTriplestoreContext) iidmToCgmesMapper
                .get(change.getAttribute());
            cgmesPredicate = mapTriplestoreContext.getAttributeName();
            cgmesContext = mapTriplestoreContext.getContext();
        } else {
//            // for onCreate all fields are inside the Identifiable object.
//            // We dont know which they are. So we will itearte over the corresponding
//            // element mapper, to get all informed fields.
//            Iterator entries = iidmToCgmesMapper.entrySet().iterator();
//            while (entries.hasNext()) {
//                Map.Entry entry = (Map.Entry) entries.next();
//                String iidmAtribute = (String) entry.getKey();
//                MapTriplestoreContext cgmesAttribute = (MapTriplestoreContext) entry.getValue(); // "cim:IdentifiedObject.name"
//                LOG.info("Key = " + iidmAtribute + ", Value = " + cgmesAttribute.getAttributeName());
//            }
            MapTriplestoreContext mapTriplestoreContext = (MapTriplestoreContext) iidmToCgmesMapper.get("name");
            cgmesPredicate = mapTriplestoreContext.getAttributeName();
            cgmesContext = mapTriplestoreContext.getContext();
            rdfType = iidmInstanceToRdfTypeMapper().get(iidmInstanceName);
        }

        if (change.getNewValueString() != null) {
            cgmesNewValue = change.getNewValueString();
        } else {
            cgmesNewValue = change.getIdentifiable().getName();
        }

        cgmesChanges = Collections.unmodifiableMap(Stream.of(
            entry("cgmesSubject", cgmesSubject),
            entry("cgmesPredicate", cgmesPredicate),
            entry("cgmesNewValue", cgmesNewValue),
            entry("cgmesContext", cgmesContext),
            entry("rdfType",rdfType)).collect(entriesToMap()));

        return cgmesChanges;
    }

    private String getIidmInstanceName() {
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }
    
    public static Map<String, String> iidmInstanceToRdfTypeMapper(){
        return Collections.unmodifiableMap(Stream.of(
            entry("GeneratorImpl","cim:SynchronousMachine"),
            entry("LoadImpl","cim:EnergyConsumer"),
            entry("SubstationImpl", "cim:Substation")).collect(entriesToMap()));
    }

//    public static Map<Object, String> generatorToSynchronousMachine() {
//        return Collections.unmodifiableMap(Stream.of(
//            entry("name", "name"),
//            entry("minP", "minOperatingP"),
//            entry("maxP", "maxOperatingP"),
//            entry("minQ", "minQ"),
//            entry("maxQ", "maxQ"),
//            entry("qPercent", "qPercent"),
//            entry("ratedS", "ratedS"),
//            entry("targetP", "nominalP"),
//            entry("targetQ", "q"),
//            entry("GeneratingUnit", "MemberOf_GeneratingUnit")).collect(entriesToMap()));
//    }
//
//    public static Map<Object, String> loadToEnergyConsumer() {
//        return Collections.unmodifiableMap(Stream.of(
//            entry("name", "name"),
//            entry("p0", "pfixed"),
//            entry("q0", "qfixed"),
//            entry("VoltageLevel", "MemberOf_EquipmentContainer")).collect(entriesToMap()));
//    }

    public static Map<String, Object> substationToSubstation() {
        return Collections.unmodifiableMap(Stream.of(
            entry("name", new MapTriplestoreContext("cim:IdentifiedObject.name", "_EQ")),
            entry("subRegionName", new MapTriplestoreContext("cim:SubGeographicalRegion.Region", "_EQ")),
            entry("country", new MapTriplestoreContext("cim:Substation.Region", "_EQ"))).collect(entriesToMap()));
    }

//    public static Map<Object, String> voltageLevelToVoltageLevel() {
//        return Collections.unmodifiableMap(Stream.of(
//            entry("name", "name"),
//            entry("BaseVoltage", "BaseVoltage"),
//            entry("highVoltageLimit", "highVoltageLimit"),
//            entry("lowVoltageLimit", "lowVoltageLimit"),
//            entry("nominalV", "nominalVoltage"),
//            entry("Substation", "MemberOf_Substation")).collect(entriesToMap()));
//    }

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    public static <String, Object> Map.Entry<String, Object> entry(String key, Object value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <String, Object> Collector<Map.Entry<String, Object>, ?, Map<String, Object>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    private IidmChange change;
    private Map<String, String> cgmesChanges;
    private Map<String, Object> iidmToCgmesMapper;
    private String iidmInstanceName;
    private static String cgmesSubject;
    private final NamingStrategy namingStrategy;
    
    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
