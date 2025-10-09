/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;

import java.util.*;

import static com.powsybl.iidm.network.extensions.util.DiscreteMeasurementValidationUtil.checkValue;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class DiscreteMeasurementImpl implements DiscreteMeasurement {

    private final DiscreteMeasurementsImpl<? extends Identifiable<?>> discreteMeasurements;
    private final String id;
    private final DiscreteMeasurement.Type type;
    private final DiscreteMeasurement.TapChanger tapChanger;
    private final Map<String, String> properties = new HashMap<>();

    private ValueType valueType;
    private Object value;
    private boolean valid;

    DiscreteMeasurementImpl(DiscreteMeasurementsImpl<? extends Identifiable<?>> discreteMeasurements, String id, DiscreteMeasurement.Type type,
                            DiscreteMeasurement.TapChanger tapChanger, Map<String, String> properties, ValueType valueType, Object value,
                            boolean valid) {
        this.discreteMeasurements = Objects.requireNonNull(discreteMeasurements);
        this.id = id;
        this.type = type;
        this.tapChanger = tapChanger;
        this.properties.putAll(properties);
        this.valueType = Objects.requireNonNull(valueType);
        this.value = value;
        this.valid = valid;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public TapChanger getTapChanger() {
        return tapChanger;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public DiscreteMeasurement putProperty(String name, String property) {
        properties.put(Objects.requireNonNull(name), property);
        return this;
    }

    @Override
    public DiscreteMeasurement removeProperty(String name) {
        properties.remove(name);
        return this;
    }

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    @Override
    public String getValueAsString() {
        if (valueType == ValueType.STRING) {
            return (String) value;
        }
        throw new PowsyblException("Value type is not STRING but is: " + valueType.name());
    }

    @Override
    public int getValueAsInt() {
        if (valueType == ValueType.INT) {
            return (int) value;
        }
        throw new PowsyblException("Value type is not INT but is: " + valueType.name());
    }

    @Override
    public boolean getValueAsBoolean() {
        if (valueType == ValueType.BOOLEAN) {
            return (boolean) value;
        }
        throw new PowsyblException("Value type is not BOOLEAN but is: " + valueType.name());
    }

    @Override
    public DiscreteMeasurement setValue(String value) {
        checkValue(value, valid);
        valueType = ValueType.STRING;
        this.value = value;
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(int value) {
        valueType = ValueType.INT;
        this.value = value;
        return this;
    }

    @Override
    public DiscreteMeasurement setValue(boolean value) {
        valueType = ValueType.BOOLEAN;
        this.value = value;
        return this;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public DiscreteMeasurement setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    @Override
    public void remove() {
        discreteMeasurements.remove(this);
    }
}
