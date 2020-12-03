/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DanglingLineBoundaryImpl implements Boundary {

    private final DanglingLine parent;

    DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideU(parent);
    }

    @Override
    public double getAngle() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideA(parent);
    }

    @Override
    public double getP() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideP(parent);
    }

    @Override
    public double getQ() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideQ(parent);
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
