/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageAngleLimitAdderImpl implements VoltageAngleLimitAdder {

    private final Ref<NetworkImpl> networkRef;
    private String name;
    private TerminalRef from;
    private TerminalRef to;
    private double lowLimit = Double.NaN;

    private double highLimit = Double.NaN;

    VoltageAngleLimitAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
    }

    @Override
    public VoltageAngleLimitAdderImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl from(TerminalRef from) {
        this.from = from;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl to(TerminalRef to) {
        this.to = to;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl setLowLimit(double lowLimit) {
        this.lowLimit = lowLimit;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl setHighLimit(double highLimit) {
        this.highLimit = highLimit;
        return this;
    }

    @Override
    public VoltageAngleLimit add() {
        if (name == null) {
            throw new IllegalStateException("Voltage angle limit name is mandatory.");
        }
        if (!Double.isNaN(lowLimit) && !Double.isNaN(highLimit) && lowLimit >= highLimit) {
            throw new IllegalStateException("Voltage angle low limit must be lower than the high limit.");
        }
        TerminalRef.Side fromSide = from.getSide().orElse(null);
        Terminal fromTerminal = TerminalRef.resolve(from.getId(), fromSide, networkRef.get());

        TerminalRef.Side toSide = to.getSide().orElse(null);
        Terminal toTerminal = TerminalRef.resolve(to.getId(), toSide, networkRef.get());

        VoltageAngleLimit voltageAngleLimit = new VoltageAngleLimitImpl(name, fromTerminal, toTerminal, lowLimit, highLimit);
        networkRef.get().getVoltageAngleLimits().add(voltageAngleLimit);
        return voltageAngleLimit;
    }
}
