/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.powsybl.iidm.network.extensions.util.DiscreteMeasurementValidationUtil.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteMeasurementAdderImpl implements DiscreteMeasurementAdder {

    private final DiscreteMeasurementsImpl<? extends Identifiable<?>> discreteMeasurements;
    private final Map<String, Object> properties = new HashMap<>();

    private String id;
    private DiscreteMeasurement.Type type;
    private DiscreteMeasurement.TapChanger tapChanger;
    private String valueAsString;
    private int valueAsInt = -1;
    private boolean valid = true;

    DiscreteMeasurementAdderImpl(DiscreteMeasurementsImpl<? extends Identifiable<?>> discreteMeasurements) {
        this.discreteMeasurements = Objects.requireNonNull(discreteMeasurements);
    }

    @Override
    public DiscreteMeasurementAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder putProperty(String name, Object value) {
        properties.put(Objects.requireNonNull(name), value);
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setType(DiscreteMeasurement.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setTapChanger(DiscreteMeasurement.TapChanger tapChanger) {
        this.tapChanger = tapChanger;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setStringValue(String value) {
        this.valueAsString = value;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setIntValue(int value) {
        this.valueAsInt = value;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    @Override
    public DiscreteMeasurement add() {
        checkId(id, discreteMeasurements);
        checkType(type, discreteMeasurements.getExtendable());
        checkTapChanger(tapChanger, type, discreteMeasurements.getExtendable());
        checkValues(valueAsString, valueAsInt);
        DiscreteMeasurementImpl discreteMeasurement = new DiscreteMeasurementImpl(discreteMeasurements, id, type, tapChanger, properties, valueAsString, valueAsInt, valid);
        discreteMeasurements.add(discreteMeasurement);
        return discreteMeasurement;
    }
}
