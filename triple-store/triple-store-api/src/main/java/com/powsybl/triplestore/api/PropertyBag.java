/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class PropertyBag extends HashMap<String, String> {

    public PropertyBag(List<String> propertyNames) {
        this(propertyNames, false);
    }

    public PropertyBag(List<String> propertyNames, boolean removeUnderscore) {
        super(propertyNames.size());
        this.propertyNames = propertyNames;
        this.removeInitialUnderscoreForIdentifiers = removeUnderscore;
    }

    public List<String> propertyNames() {
        return propertyNames;
    }

    public String getLocal(String property) {
        String value = get(property);
        if (value == null) {
            return null;
        }
        return value.replaceAll("^.*#", "");
    }

    public String getId(String property) {
        String value = get(property);
        if (value == null) {
            return null;
        }
        // rdf:ID is the mRID plus an underscore added at the beginning of the string
        // We may decide if we want to preserve or not the underscore
        if (removeInitialUnderscoreForIdentifiers) {
            return value.replaceAll("^.*#_?", "");
        } else {
            return value.replaceAll("^.*#", "");
        }
    }

    public String getId0(String property) {
        // Return the first part of the Id (before he first hyphen)
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
        return Double.parseDouble(get(property));
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
            return title.concat(lineSeparator).concat(propertyNames.stream()
                    .map(n -> INDENTATION.concat(padr(n, lenPad)).concat(" : ")
                            + getValue.apply(this, n))
                    .collect(Collectors.joining(lineSeparator)));
        }
        return "";
    }

    private static String padr(String s, int size) {
        String format = String.format("%%-%ds", size);
        return String.format(format, s);
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
        if (!(obj instanceof PropertyBag)) {
            return false;
        }
        PropertyBag p = (PropertyBag) obj;
        if (removeInitialUnderscoreForIdentifiers != p.removeInitialUnderscoreForIdentifiers) {
            return false;
        }
        return propertyNames.equals(p.propertyNames);
    }

    public boolean isResource(String name) {
        // TODO do not rely on property name, use metadata or answer based on value?
        return name.equals("TopologicalNode") || name.equals("Terminal")
                || name.equals("ShuntCompensator") || name.equals("TapChanger");
    }

    public String namespacePrefix(String name) {
        LOG.trace("namespacePrefix for property name {}", name);
        return NAMESPACE_PREFIX;
    }

    private final List<String> propertyNames;
    private final boolean removeInitialUnderscoreForIdentifiers;

    private static final String NAMESPACE_PREFIX = "data";
    private static final String INDENTATION = "    ";

    private static final Logger LOG = LoggerFactory.getLogger(PropertyBag.class);
}
