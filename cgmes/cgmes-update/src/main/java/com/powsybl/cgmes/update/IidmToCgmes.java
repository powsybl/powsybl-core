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

import com.powsybl.cgmes.conversion.NamingStrategy;

public class IidmToCgmes {
    // responsible for mapping back identifiers and attribute names.

    public IidmToCgmes(IidmChangeOnUpdate change) {
        this.change = change;
        this.namingStrategy = new NamingStrategy.Identity();
    }

    public Map<String, String> convert(IidmChangeOnUpdate change) {
        cgmesChanges = new HashMap<String, String>();

        String iidmIdentifiableId = change.getIdentifiableId();
        String idmAttribute = change.getAttribute();
        String iidmOldValue = change.getOldValueString();
        String iidmNewValue = change.getNewValueString();

        String cgmesSubject = namingStrategy.getCgmesId(iidmIdentifiableId);
        String cgmesPredicate = idmAttribute;
        String cgmesOldValue = iidmOldValue;
        String cgmesNewValue = iidmNewValue;
        cgmesChanges.put("cgmesSubject", cgmesSubject);
        cgmesChanges.put("cgmesPredicate", cgmesPredicate);
        cgmesChanges.put("cgmesNewValue", cgmesNewValue);

        LOG.info("Triple to change: " + cgmesChanges.entrySet());

        return cgmesChanges;
    }

    private String getIidmInstanceName() {

        LOG.info(change.getIdentifiable().getClass().getSimpleName());
        return change.getIdentifiable().getClass().getSimpleName();
    }

    private Map<String, Map<Object, String>> iidmCgmesElementsMapping() {
        // map Identifiable Class with its method Map
        return Collections.unmodifiableMap(Stream.of(
            entry("GeneratorImpl", generatorImpl()),
            entry("Load", load())).collect(entriesToMap()));
    }

    public static Map<Object, String> generatorImpl() {

        return Collections.unmodifiableMap(Stream.of(
            entry("minP", "minP"),
            entry("maxP", "maxP"),
            entry("qPercent", "qPercent"),
            entry("ratedS", "ratedS")).collect(entriesToMap()));
    }

    public static Map<Object, String> load() {

        return Collections.unmodifiableMap(Stream.of(
            entry("minP", "minP"),
            entry("ratedS", "ratedS")).collect(entriesToMap()));
    }

    // http://minborgsjavapot.blogspot.com/2014/12/java-8-initializing-maps-in-smartest-way.html
    public static <K, V> Map.Entry<K, V> entry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
    }

    private IidmChangeOnUpdate change;
    private Map<String, String> cgmesChanges;
    private static Object cgmesSubject;
    private final NamingStrategy namingStrategy;
    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
