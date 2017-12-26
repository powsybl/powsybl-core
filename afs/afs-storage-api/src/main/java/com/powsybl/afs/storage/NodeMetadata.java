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
public class NodeMetadata {

    private final Map<String, String> stringMetadata = new HashMap<>();

    private final Map<String, Double> doubleMetadata = new HashMap<>();

    private final Map<String, Integer> intMetadata = new HashMap<>();

    private final Map<String, Boolean> booleanMetadata = new HashMap<>();

    public NodeMetadata setStringMetadata(String name, String value) {
        stringMetadata.put(name, value);
        return this;
    }

    public Map<String, String> getStringMetadata() {
        return stringMetadata;
    }

    public NodeMetadata setDoubleMetadata(String name, double value) {
        doubleMetadata.put(name, value);
        return this;
    }

    public Map<String, Double> getDoubleMetadata() {
        return doubleMetadata;
    }

    public NodeMetadata setIntMetadata(String name, int value) {
        intMetadata.put(name, value);
        return this;
    }

    public Map<String, Integer> getIntMetadata() {
        return intMetadata;
    }

    public NodeMetadata setBooleanMetadata(String name, boolean value) {
        booleanMetadata.put(name, value);
        return this;
    }

    public Map<String, Boolean> getBooleanMetadata() {
        return booleanMetadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringMetadata, doubleMetadata, intMetadata, booleanMetadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeMetadata) {
            NodeMetadata other = (NodeMetadata) obj;
            return stringMetadata.equals(other.stringMetadata) && doubleMetadata.equals(other.doubleMetadata) &&
                    intMetadata.equals(other.intMetadata) && booleanMetadata.equals(other.booleanMetadata);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeMetadata(stringMetadata=" + stringMetadata + ", doubleMetadata=" + doubleMetadata +
                ", intMetadata=" + intMetadata + ", booleanMetadata=" + booleanMetadata + ")";
    }
}
