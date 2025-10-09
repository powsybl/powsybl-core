/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class DiscreteMeasurementAdderImpl implements DiscreteMeasurementAdder {

    private final DiscreteMeasurementsImpl<? extends Identifiable<?>> discreteMeasurements;
    private final Map<String, String> properties = new HashMap<>();

    private String id;
    private boolean idUnicity = false;
    private DiscreteMeasurement.Type type;
    private DiscreteMeasurement.TapChanger tapChanger;
    private Object value = null;
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
    public DiscreteMeasurementAdder putProperty(String name, String value) {
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
    public DiscreteMeasurementAdder setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setValue(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setValue(int value) {
        this.value = value;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    @Override
    public DiscreteMeasurementAdder setEnsureIdUnicity(boolean idUnicity) {
        this.idUnicity = idUnicity;
        return this;
    }

    @Override
    public DiscreteMeasurement add() {
        id = checkId(id, idUnicity, discreteMeasurements);
        checkType(type, discreteMeasurements.getExtendable());
        checkTapChanger(tapChanger, type, discreteMeasurements.getExtendable());
        checkValue(value, valid);
        DiscreteMeasurementImpl discreteMeasurement = new DiscreteMeasurementImpl(discreteMeasurements, id, type, tapChanger, properties, getValueType(value), value, valid);
        discreteMeasurements.add(discreteMeasurement);
        return discreteMeasurement;
    }
}
