/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteMeasurementImpl implements DiscreteMeasurement {

    private final DiscreteMeasurementsImpl discreteMeasurements;
    private final String id;
    private final DiscreteMeasurement.Type type;
    private final DiscreteMeasurement.TapChanger tapChanger;
    private final Properties properties = new Properties();

    private String valueAsString;
    private int valueAsInt;
    private boolean valid;

    DiscreteMeasurementImpl(DiscreteMeasurementsImpl discreteMeasurements, String id, DiscreteMeasurement.Type type,
                            DiscreteMeasurement.TapChanger tapChanger, Properties properties, String valueAsString, int valueAsInt,
                            boolean valid) {
        this.discreteMeasurements = Objects.requireNonNull(discreteMeasurements);
        this.id = id;
        this.type = type;
        this.tapChanger = tapChanger;
        this.properties.putAll(properties);
        this.valueAsString = Objects.requireNonNullElseGet(valueAsString, () -> String.valueOf(valueAsInt));
        this.valid = valid;
        this.valueAsInt = valueAsInt;
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
    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

    @Override
    public DiscreteMeasurement putProperty(String name, Object property) {
        properties.put(Objects.requireNonNull(name), property);
        return this;
    }

    @Override
    public String getValueAsString() {
        return valueAsString;
    }

    @Override
    public int getValueAsInt() {
        return valueAsInt;
    }

    @Override
    public DiscreteMeasurement setValue(String valueAsString, int valueAsInt) {
        checkValues(valueAsString, valueAsInt);
        this.valueAsString = valueAsString;
        this.valueAsInt = valueAsInt;
        return null;
    }

    @Override
    public DiscreteMeasurement setValue(String value) {
        return setValue(value, -1);
    }

    @Override
    public DiscreteMeasurement setValue(int value) {
        return setValue(null, value);
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

    private static void checkValues(String valueAsString, int valueAsInt) {
        if (valueAsString == null && valueAsInt == -1) {
            throw new PowsyblException("A string or an integer value must be defined for DiscreteMeasurement");
        }
    }
}
