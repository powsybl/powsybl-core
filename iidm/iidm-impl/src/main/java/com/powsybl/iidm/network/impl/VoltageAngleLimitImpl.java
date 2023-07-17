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

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class VoltageAngleLimitImpl implements VoltageAngleLimit {

    VoltageAngleLimitImpl(String name, Terminal terminalFrom, Terminal terminalTo, double limit, FlowDirection flowDirection) {
        this.name = name;
        this.terminalFrom = terminalFrom;
        this.terminalTo = terminalTo;
        this.limit = limit;
        this.flowDirection = flowDirection;
    }

    @Override
    public Optional<String> getOptionalName() {
        return Optional.ofNullable(name);
    }

    @Override
    public Terminal getTerminalFrom() {
        return terminalFrom;
    }

    @Override
    public Terminal getTerminalTo() {
        return terminalTo;
    }

    @Override
    public double getLimit() {
        return limit;
    }

    @Override
    public FlowDirection getFlowDirection() {
        return flowDirection;
    }

    private String name;
    private Terminal terminalFrom;
    private Terminal terminalTo;
    private double limit;
    private FlowDirection flowDirection;
}
