/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.Optional;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.TerminalRef.Side;
import com.powsybl.iidm.network.VoltageAngleLimit.FlowDirection;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageAngleLimitAdderImpl implements VoltageAngleLimitAdder {

    private final Ref<NetworkImpl> networkRef;
    private TerminalRef from;
    private TerminalRef to;
    private double limit = Double.NaN;
    private FlowDirection flowDirection = FlowDirection.BOTH_DIRECTIONS;

    VoltageAngleLimitAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
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
        Optional<Identifiable<?>> identifiableFrom = getIdentifiable(networkRef.get(), from);
        if (identifiableFrom.isEmpty()) {
            throw new IllegalStateException("Identifiable from not found: " + from.getId());
        }
        Optional<Identifiable<?>> identifiableTo = getIdentifiable(networkRef.get(), to);
        if (identifiableTo.isEmpty()) {
            throw new IllegalStateException("Identifiable to not found: " + to.getId());
        }
        Terminal terminalFrom = getTerminal(identifiableFrom.get(), from.getSide());
        Switch switchFrom = getSwitch(identifiableFrom.get());
        Terminal terminalTo = getTerminal(identifiableTo.get(), to.getSide());
        Switch switchTo = getSwitch(identifiableTo.get());
        VoltageAngleLimit voltageAngleLimit = new VoltageAngleLimitImpl(from, to, terminalFrom, terminalTo, switchFrom, switchTo, limit, flowDirection);
        networkRef.get().getVoltageAngleLimits().add(voltageAngleLimit);
        return voltageAngleLimit;
    }

    private static Optional<Identifiable<?>> getIdentifiable(Network network, TerminalRef terminalRef) {
        return Optional.ofNullable(network.getIdentifiable(terminalRef.getId()));
    }

    private static Switch getSwitch(Identifiable<?> identifiable) {
        if (identifiable instanceof Switch) {
            return (Switch) identifiable;
        } else {
            return null;
        }
    }

    private static Terminal getTerminal(Identifiable<?> identifiable, Side side) {
        if (identifiable instanceof Switch) {
            return null;
        } else if (identifiable instanceof HvdcLine) {
            throw new IllegalStateException("HvdcLines do not have terminales : " + identifiable.getId());
        } else if (identifiable instanceof Connectable) {
            return getTerminal((Connectable<?>) identifiable, side);
        } else {
            throw new IllegalStateException();
        }
    }

    private static Terminal getTerminal(Connectable<?> connectable, Side side) {
        if (connectable instanceof Injection) {
            return ((Injection<?>) connectable).getTerminal();
        } else if (connectable instanceof Branch) {
            if (side.equals(Side.ONE)) {
                return ((Branch<?>) connectable).getTerminal1();
            } else if (side.equals(Side.TWO)) {
                return ((Branch<?>) connectable).getTerminal2();
            } else {
                throw new IllegalStateException("Unexpected Branch side: " + side.name());
            }
        } else if (connectable instanceof ThreeWindingsTransformer) {
            if (side.equals(Side.ONE)) {
                return ((ThreeWindingsTransformer) connectable).getLeg1().getTerminal();
            } else if (side.equals(Side.TWO)) {
                return ((ThreeWindingsTransformer) connectable).getLeg2().getTerminal();
            } else {
                return ((ThreeWindingsTransformer) connectable).getLeg3().getTerminal();
            }
        } else {
            throw new IllegalStateException();
        }
    }
}
