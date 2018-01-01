/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeGenericMetadata {

    private final Map<String, String> stringMetadata;

    private final Map<String, Double> doubleMetadata;

    private final Map<String, Integer> intMetadata;

    private final Map<String, Boolean> booleanMetadata;

    public NodeGenericMetadata() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public NodeGenericMetadata(Map<String, String> stringMetadata, Map<String, Double> doubleMetadata, Map<String, Integer> intMetadata,
                               Map<String, Boolean> booleanMetadata) {
        this.stringMetadata = Objects.requireNonNull(stringMetadata);
        this.doubleMetadata = Objects.requireNonNull(doubleMetadata);
        this.intMetadata = Objects.requireNonNull(intMetadata);
        this.booleanMetadata = Objects.requireNonNull(booleanMetadata);
    }

    public NodeGenericMetadata setString(String name, String value) {
        stringMetadata.put(name, value);
        return this;
    }

    public Map<String, String> getStrings() {
        return stringMetadata;
    }

    public String getString(String name) {
        Objects.requireNonNull(name);
        String value = stringMetadata.get(name);
        if (value == null) {
            throw new IllegalArgumentException("String metadata '" + name + "' not found");
        }
        return value;
    }

    public NodeGenericMetadata setDouble(String name, double value) {
        doubleMetadata.put(name, value);
        return this;
    }

    public double getDouble(String name) {
        Objects.requireNonNull(name);
        Double value = doubleMetadata.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Double metadata '" + name + "' not found");
        }
        return value;
    }

    public Map<String, Double> getDoubles() {
        return doubleMetadata;
    }

    public NodeGenericMetadata setInt(String name, int value) {
        intMetadata.put(name, value);
        return this;
    }

    public int getInt(String name) {
        Objects.requireNonNull(name);
        Integer value = intMetadata.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Integer metadata '" + name + "' not found");
        }
        return value;
    }

    public Map<String, Integer> getInts() {
        return intMetadata;
    }

    public NodeGenericMetadata setBoolean(String name, boolean value) {
        booleanMetadata.put(name, value);
        return this;
    }

    public boolean getBoolean(String name) {
        Objects.requireNonNull(name);
        Boolean value = booleanMetadata.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Boolean metadata '" + name + "' not found");
        }
        return value;
    }

    public Map<String, Boolean> getBooleans() {
        return booleanMetadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringMetadata, doubleMetadata, intMetadata, booleanMetadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeGenericMetadata) {
            NodeGenericMetadata other = (NodeGenericMetadata) obj;
            return stringMetadata.equals(other.stringMetadata) && doubleMetadata.equals(other.doubleMetadata) &&
                    intMetadata.equals(other.intMetadata) && booleanMetadata.equals(other.booleanMetadata);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeGenericMetadata(stringMetadata=" + stringMetadata + ", doubleMetadata=" + doubleMetadata +
                ", intMetadata=" + intMetadata + ", booleanMetadata=" + booleanMetadata + ")";
    }
}
