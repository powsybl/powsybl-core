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

import com.powsybl.cgmes.conversion.NamingStrategy;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.

    public IidmToCgmes(IidmChangeOnUpdate change) {
        this.change = change;
        this.namingStrategy = new NamingStrategy.Identity();
    }

    public Map<String, String> convert(IidmChangeOnUpdate change) throws Exception {

        cgmesChanges = new HashMap<String, String>();

        // map Identifiable Instance with its method for IIDM - CGMES conversion
        Map<String, Callable<Map<Object, String>>> getConversionMapper = new HashMap<>();
        getConversionMapper.put("GeneratorImpl", () -> generatorToSynchronousMachine());
        getConversionMapper.put("LoadImpl", () -> loadToEnergyConsumer());
        getConversionMapper.put("LoadImpl", () -> substationToSubstation());

        // Getting IIDM - CGMES conversion mapper:
        iidmInstance = getIidmInstanceName();
        iidmToCgmesMapper = getConversionMapper.get(iidmInstance).call();
        LOG.info("The iidmToCgmesMapper result is: " + iidmToCgmesMapper.entrySet().toString());

        String cgmesSubject = namingStrategy.getCgmesId(change.getIdentifiableId());
        String cgmesPredicate = iidmToCgmesMapper.get(change.getAttribute());
        String cgmesNewValue = change.getNewValueString();

        cgmesChanges = Collections.unmodifiableMap(Stream.of(
            entry("cgmesSubject", cgmesSubject),
            entry("cgmesPredicate", cgmesPredicate),
            entry("cgmesNewValue", cgmesNewValue)).collect(entriesToMap()));

        return cgmesChanges;
    }

    private String getIidmInstanceName() {
        LOG.info("IIDM instance is: " + change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    public static Map<Object, String> generatorToSynchronousMachine() {
        return Collections.unmodifiableMap(Stream.of(
            entry("minP", "minP"),
            entry("maxP", "maxP"),
            entry("qPercent", "qPercent"),
            entry("ratedS", "ratedS")).collect(entriesToMap()));
    }

    public static Map<Object, String> loadToEnergyConsumer() {
        return Collections.unmodifiableMap(Stream.of(
            entry("p0", "pfixed"),
            entry("q0", "qfixed")).collect(entriesToMap()));
    }

    public static Map<Object, String> substationToSubstation() {
        return Collections.unmodifiableMap(Stream.of(
            entry("SubRegion", "SubRegion"),
            entry("subRegionName", "subRegionName")).collect(entriesToMap()));
    }

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    public static <Object, String> Map.Entry<Object, String> entry(Object key, String value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <Object, String> Collector<Map.Entry<Object, String>, ?, Map<Object, String>> entriesToMap() {
        return Collectors.toMap(e -> e.getKey(), e -> e.getValue());
    }

    private IidmChangeOnUpdate change;
    private Map<String, String> cgmesChanges;
    private Map<Object, String> iidmToCgmesMapper;
    private String iidmInstance;
    private static String cgmesSubject;
    private final NamingStrategy namingStrategy;
    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
