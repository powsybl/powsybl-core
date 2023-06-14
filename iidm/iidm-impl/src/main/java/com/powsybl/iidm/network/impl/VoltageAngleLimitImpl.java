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
import com.powsybl.iidm.network.util.SwitchTerminalForVoltage;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageAngleLimitImpl implements VoltageAngleLimit {

    VoltageAngleLimitImpl(TerminalRef from, TerminalRef to, Terminal terminalFrom, Terminal terminalTo,
        Switch switchFrom, Switch switchTo, double limit, FlowDirection flowDirection) {
        this.from = from;
        this.terminalFrom = terminalFrom;
        this.switchFrom = switchFrom;
        this.to = to;
        this.terminalTo = terminalTo;
        this.switchTo = switchTo;
        this.limit = limit;
        this.flowDirection = flowDirection;
    }

    @Override
    public TerminalRef getFrom() {
        return from;
    }

    @Override
    public Optional<Terminal> getTerminalFrom() {
        if (terminalFrom != null) {
            return Optional.of(terminalFrom);
        }
        if (switchFrom != null) {
            return getTerminal(switchFrom, from.getSide());
        }
        return Optional.empty();
    }

    @Override
    public TerminalRef getTo() {
        return to;
    }

    @Override
    public Optional<Terminal> getTerminalTo() {
        if (terminalTo != null) {
            return Optional.of(terminalTo);
        }
        if (switchTo != null) {
            return getTerminal(switchTo, to.getSide());
        }
        return Optional.empty();
    }

    @Override
    public double getLimit() {
        return limit;
    }

    @Override
    public FlowDirection getFlowDirection() {
        return flowDirection;
    }

    private static Optional<Terminal> getTerminal(Switch sw, Side side) {
        if (side.equals(Side.ONE)) {
            return new SwitchTerminalForVoltage(sw).getTerminal1();
        } else if (side.equals(Side.TWO)) {
            return new SwitchTerminalForVoltage(sw).getTerminal1();
        } else {
            return Optional.empty();
        }
    }

    private TerminalRef from;
    private TerminalRef to;
    private Terminal terminalFrom;
    private Switch switchFrom;
    private Terminal terminalTo;
    private Switch switchTo;
    private double limit;
    private FlowDirection flowDirection;
}
