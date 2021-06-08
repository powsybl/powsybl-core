/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.Analog;
import com.powsybl.iidm.network.extensions.AnalogAdder;
import com.powsybl.iidm.network.extensions.Analogs;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class AnalogAdderImpl implements AnalogAdder {

    private final AnalogsImpl analogs;
    private final Properties properties = new Properties();

    private String id;
    private Analog.Type type;
    private double value;
    private Analog.Side side;

    AnalogAdderImpl(AnalogsImpl analogs) {
        this.analogs = Objects.requireNonNull(analogs);
    }

    @Override
    public AnalogAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public AnalogAdder putProperty(String name, Object property) {
        properties.put(name, property);
        return this;
    }

    @Override
    public AnalogAdder setType(Analog.Type type) {
        this.type = type;
        return this;
    }

    @Override
    public AnalogAdder setValue(double value) {
        this.value = value;
        return this;
    }

    @Override
    public AnalogAdder setSide(Analog.Side side) {
        this.side = side;
        return this;
    }

    @Override
    public Analogs add() {
        if (type == null) {
            throw new PowsyblException("Analog type can not be null");
        }
        checkValue(value);
        checkSide(side, (Connectable) analogs.getExtendable());
        return analogs.addAnalog(new AnalogImpl(id, type, properties, value, side));
    }

    private static void checkValue(double value) {
        if (Double.isNaN(value)) {
            throw new PowsyblException("Undefined value for analog");
        }
    }

    private static void checkSide(Analog.Side side, Connectable c) {
        if (side == null) {
            if (c instanceof Branch || c instanceof ThreeWindingsTransformer) {
                throw new PowsyblException("Inconsistent null side for analog of branch or three windings transformer");
            }
        } else {
            if (c instanceof Injection) {
                throw new PowsyblException("Inconsistent side for analog of injection");
            }
        }
    }
}
