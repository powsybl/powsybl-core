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
    public VoltageAngleLimitAdderImpl setReferenceTerminal(TerminalRef from) {
        this.from = from;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl setOtherTerminal(TerminalRef to) {
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
            throw new IllegalStateException("VoltageAngleLimit name is mandatory.");
        }
        if (!Double.isNaN(lowLimit) && !Double.isNaN(highLimit) && lowLimit >= highLimit) {
            throw new IllegalStateException("VoltageAngleLimit lowLimit must be inferior to highLimit.");
        }
        TerminalRef.Side sideFrom = from.getSide().orElse(TerminalRef.Side.ONE);
        Terminal terminalFrom = TerminalRef.resolve(from.getId(), sideFrom, networkRef.get());

        TerminalRef.Side sideTo = to.getSide().orElse(TerminalRef.Side.ONE);
        Terminal terminalTo = TerminalRef.resolve(to.getId(), sideTo, networkRef.get());

        if (terminalFrom.getConnectable().getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            throw new IllegalStateException("VoltageAngleLimit can not be defined on threeWindingsTransformers : " + terminalFrom.getConnectable().getId());
        }
        if (terminalTo.getConnectable().getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            throw new IllegalStateException("VoltageAngleLimit can not be defined on threeWindingsTransformers : " + terminalTo.getConnectable().getId());
        }

        VoltageAngleLimit voltageAngleLimit = new VoltageAngleLimitImpl(name, terminalFrom, terminalTo, lowLimit, highLimit);
        networkRef.get().getVoltageAngleLimits().add(voltageAngleLimit);
        return voltageAngleLimit;
    }
}
