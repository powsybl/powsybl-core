/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageAngleLimit.FlowDirection;
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
    private double limit = Double.NaN;
    private FlowDirection flowDirection = FlowDirection.BOTH_DIRECTIONS;

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
    public VoltageAngleLimitAdderImpl withLimit(double limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public VoltageAngleLimitAdderImpl withFlowDirection(FlowDirection flowDirection) {
        this.flowDirection = flowDirection;
        return this;
    }

    @Override
    public VoltageAngleLimit add() {
        if (limit <= 0) {
            throw new IllegalStateException("Limit <= 0: " + Double.toString(limit));
        }
        Terminal terminalFrom = TerminalRef.resolve(from.getId(), from.getSide(), networkRef.get());
        Terminal terminalTo = TerminalRef.resolve(to.getId(), to.getSide(), networkRef.get());

        if (terminalFrom.getConnectable().getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            throw new IllegalStateException("VoltageAngleLimit can not be defined on threeWindingsTransformers : " + terminalFrom.getConnectable().getId());
        }
        if (terminalTo.getConnectable().getType().equals(IdentifiableType.THREE_WINDINGS_TRANSFORMER)) {
            throw new IllegalStateException("VoltageAngleLimit can not be defined on threeWindingsTransformers : " + terminalTo.getConnectable().getId());
        }

        VoltageAngleLimit voltageAngleLimit = new VoltageAngleLimitImpl(name, terminalFrom, terminalTo, limit, flowDirection);
        networkRef.get().getVoltageAngleLimits().add(voltageAngleLimit);
        return voltageAngleLimit;
    }
}
