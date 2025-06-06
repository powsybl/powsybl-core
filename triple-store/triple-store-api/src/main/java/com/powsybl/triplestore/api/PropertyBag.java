/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class PropertyBag extends HashMap<String, String> {

    public PropertyBag(List<String> propertyNames, boolean decodeEscapedIdentifiers) {
        this(propertyNames, true, decodeEscapedIdentifiers);
    }

    public PropertyBag(List<String> propertyNames, boolean removeInitialUnderscoreForIdentifiers, boolean decodeEscapedIdentifiers) {
        super(propertyNames.size());
        this.propertyNames = propertyNames;
        this.removeInitialUnderscoreForIdentifiers = removeInitialUnderscoreForIdentifiers;
        this.decodeEscapedIdentifiers = decodeEscapedIdentifiers;
    }

    public List<String> propertyNames() {
        return propertyNames;
    }

    public void putNonNull(String key, String value) {
        if (key != null && value != null) {
            put(key, value);
        }
    }

    public String getLocal(String property) {
        String value = get(property);
        if (value == null) {
            return null;
        }
        return extractIdentifier(value, false);
    }

    public String[] getLocals(String property, String separator) {
        String value = get(property);
        if (value == null) {
            return new String[] {};
        }
        String[] tokens = value.split(separator);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = extractIdentifier(tokens[i], false);
        }
        return tokens;
    }

    public String getId(String property) {
        String value = get(property);
        if (value == null) {
            return null;
        }
        return extractIdentifier(value, true);
    }

    public String getId0(String property) {
        // Return the first part of the Identifier (before the first hyphen)
        String id = getId(property);
        if (id == null) {
            return null;
        }
        int h = id.indexOf('-');
        if (h < 0) {
            return id;
        }
        return id.substring(0, h);
    }

    public double asDouble(String property) {
        return asDouble(property, Double.NaN);
    }

    public double asDouble(String property, double defaultValue) {
        if (!containsKey(property)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(get(property));
        } catch (NumberFormatException x) {
            LOG.warn("Invalid value for property {} : {}", property, get(property));
            return Double.NaN;
        }
    }

    public OptionalDouble asOptionalDouble(String property) {
        if (!containsKey(property)) {
            return OptionalDouble.empty();
        }
        try {
            return OptionalDouble.of(Double.parseDouble(get(property)));
        } catch (NumberFormatException x) {
            LOG.warn("Invalid value for property {} : {}", property, get(property));
            return OptionalDouble.of(Double.NaN);
        }
    }

    public Optional<Boolean> asBoolean(String property) {
        if (!containsKey(property)) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(get(property)));
    }

    public boolean asBoolean(String property, boolean defaultValue) {
        if (!containsKey(property)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(get(property));
    }

    public int asInt(String property) {
        return Integer.parseInt(get(property));
    }

    public int asInt(String property, int defaultValue) {
        if (!containsKey(property)) {
            return defaultValue;
        }
        return Integer.parseInt(get(property));
    }

    public String tabulateLocals() {
        return tabulate("", PropertyBag::getLocal);
    }

    public String tabulate() {
        return tabulate("", PropertyBag::get);
    }

    public String tabulateLocals(String title) {
        return tabulate(title, PropertyBag::getLocal);
    }

    public String tabulate(String title) {
        return tabulate(title, HashMap::get);
    }

    private String tabulate(String title, BiFunction<PropertyBag, String, String> getValue) {
        if (size() == 0) {
            return "";
        }
        String lineSeparator = System.lineSeparator();
        Optional<Integer> maxLenName = propertyNames.stream()
                .map(String::length)
                .max(Integer::compare);
        if (maxLenName.isPresent()) {
            int lenPad = maxLenName.get();
            String format = String.format("%%-%ds", lenPad);

            // Performance : avoid using concat() -> use a StringBuilder instead.
            return new StringBuilder(title).append(lineSeparator).append(propertyNames.stream()
                .map(n -> new StringBuilder(INDENTATION).append(String.format(format, n)).append(" : ").append(getValue.apply(this, n)).toString())
                .collect(Collectors.joining(lineSeparator))).toString();
        }
        return "";
    }

    private String extractIdentifier(String s, boolean isIdentifier) {
        String s1 = s;
        int iHash = s.indexOf('#');
        if (iHash >= 0) {
            s1 = s.substring(iHash + 1);
        }
        // rdf:ID is the mRID plus an underscore added at the beginning of the string
        // We may decide if we want to preserve or not the underscore
        if (isIdentifier) {
            if (removeInitialUnderscoreForIdentifiers && !s1.isEmpty() && s1.charAt(0) == '_') {
                s1 = s1.substring(1);
            }
            if (decodeEscapedIdentifiers) {
                s1 = URLDecoder.decode(s1, StandardCharsets.UTF_8);
            }
        }
        return s1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyNames, removeInitialUnderscoreForIdentifiers);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PropertyBag p)) {
            return false;
        }
        if (removeInitialUnderscoreForIdentifiers != p.removeInitialUnderscoreForIdentifiers) {
            return false;
        }
        return propertyNames.equals(p.propertyNames);
    }

    public boolean isResource(String name) {
        return RESOURCE_NAMES.contains(name) || resourceNames.contains(name);
    }

    public String namespacePrefix(String name) {
        LOG.trace("namespacePrefix for property name {}", name);
        return NAMESPACE_PREFIX;
    }

    public void setResourceNames(List<String> resourceNames) {
        this.resourceNames.clear();
        this.resourceNames.addAll(Objects.requireNonNull(resourceNames));
    }

    public void setClassPropertyNames(List<String> classPropertyNames) {
        this.classPropertyNames.clear();
        this.classPropertyNames.addAll(Objects.requireNonNull(classPropertyNames));
    }

    public boolean isClassProperty(String name) {
        return classPropertyNames.contains(name);
    }

    public void setMultivaluedProperty(List<String> multiValuedPropertyNames) {
        this.multiValuedPropertyNames.clear();
        this.multiValuedPropertyNames.addAll(Objects.requireNonNull(multiValuedPropertyNames));
    }

    public boolean isMultivaluedProperty(String name) {
        return multiValuedPropertyNames.contains(name);
    }

    public PropertyBag copy() {
        // Create just a shallow copy of this property bag
        PropertyBag pb1 = new PropertyBag(propertyNames, removeInitialUnderscoreForIdentifiers, decodeEscapedIdentifiers);
        pb1.setResourceNames(resourceNames);
        pb1.setClassPropertyNames(classPropertyNames);
        pb1.setMultivaluedProperty(multiValuedPropertyNames);
        pb1.putAll(this);
        return pb1;
    }

    private final List<String> propertyNames;
    private final boolean removeInitialUnderscoreForIdentifiers;
    private final boolean decodeEscapedIdentifiers;
    private final List<String> resourceNames = new ArrayList<>();
    private final List<String> classPropertyNames = new ArrayList<>();
    private final List<String> multiValuedPropertyNames = new ArrayList<>();

    private static final String NAMESPACE_PREFIX = "data";
    private static final String INDENTATION = "    ";
    private static final List<String> RESOURCE_NAMES = Arrays.asList("TopologicalNode", "Terminal", "ShuntCompensator",
        "TapChanger", "ConductingEquipment", "Model.DependentOn", "TopologicalNodes",
        "AngleRefTopologicalNode");

    private static final Logger LOG = LoggerFactory.getLogger(PropertyBag.class);
}
