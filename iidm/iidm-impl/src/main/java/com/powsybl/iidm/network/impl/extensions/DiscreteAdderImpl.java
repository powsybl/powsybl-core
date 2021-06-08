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
import com.powsybl.iidm.network.extensions.Discrete;
import com.powsybl.iidm.network.extensions.DiscreteAdder;
import com.powsybl.iidm.network.extensions.Discretes;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteAdderImpl implements DiscreteAdder {

    private final DiscretesImpl discretes;
    private final Properties properties = new Properties();

    private String id;
    private Discrete.Type type;
    private String valueAsString;
    private int valueAsInt = -1;

    DiscreteAdderImpl(DiscretesImpl discretes) {
        this.discretes = Objects.requireNonNull(discretes);
    }

    @Override
    public DiscreteAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public DiscreteAdder putProperty(String name, Object value) {
        properties.put(Objects.requireNonNull(name), value);
        return this;
    }

    @Override
    public DiscreteAdder setType(Discrete.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public DiscreteAdder setStringValue(String value) {
        this.valueAsString = value;
        return this;
    }

    @Override
    public DiscreteAdder setIntValue(int value) {
        this.valueAsInt = value;
        return this;
    }

    @Override
    public Discretes add() {
        checkType(type, (Identifiable) discretes.getExtendable());
        checkValues(valueAsString, valueAsInt);
        return discretes.addDiscrete(new DiscreteImpl(id, type, properties, valueAsString, valueAsInt));
    }

    private static void checkType(Discrete.Type type, Identifiable i) {
        Objects.requireNonNull(type);
        if (type == Discrete.Type.SWITCH_POSITION && !(i instanceof Switch)) {
            throw new PowsyblException("SWITCH_POSITION discrete not linked to a switch");
        }
        if (type == Discrete.Type.TAP_POSITION && !(i instanceof TwoWindingsTransformer || i instanceof ThreeWindingsTransformer)) {
            throw new PowsyblException("TAP_POSITION discrete not linked to a transformer");
        }
    }

    private static void checkValues(String valueAsString, int valueAsInt) {
        if (valueAsString == null && valueAsInt == -1) {
            throw new PowsyblException("A string or an integer value must be defined for Discrete");
        }
    }
}
