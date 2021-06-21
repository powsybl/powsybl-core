/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteMeasurementAdderImpl implements DiscreteMeasurementAdder {

    private final DiscreteMeasurementsImpl discreteMeasurements;
    private final Properties properties = new Properties();

    private String id;
    private DiscreteMeasurement.Type type;
    private String valueAsString;
    private int valueAsInt = -1;
    private boolean valid = true;

    DiscreteMeasurementAdderImpl(DiscreteMeasurementsImpl discreteMeasurements) {
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
    public DiscreteMeasurements add() {
        checkType(type, (Identifiable) discreteMeasurements.getExtendable());
        checkValues(valueAsString, valueAsInt);
        return discreteMeasurements.add(new DiscreteMeasurementImpl(discreteMeasurements, id, type, properties, valueAsString, valueAsInt, valid));
    }

    private static void checkType(DiscreteMeasurement.Type type, Identifiable i) {
        Objects.requireNonNull(type);
        if (type == DiscreteMeasurement.Type.SWITCH_POSITION && !(i instanceof Switch)) {
            throw new PowsyblException("SWITCH_POSITION discrete not linked to a switch");
        }
        if (type == DiscreteMeasurement.Type.TAP_POSITION && !(i instanceof TwoWindingsTransformer || i instanceof ThreeWindingsTransformer)) {
            throw new PowsyblException("TAP_POSITION discrete not linked to a transformer");
        }
    }

    private static void checkValues(String valueAsString, int valueAsInt) {
        if (valueAsString == null && valueAsInt == -1) {
            throw new PowsyblException("A string or an integer value must be defined for DiscreteMeasurement");
        }
    }
}
