/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DanglingLineBoundaryImpl implements Boundary {
    // for SV use: side represents the network side, that is always
    // Side.ONE for a dangling line.

    private final DanglingLine parent;

    DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), Branch.Side.ONE).otherSideU(parent, true);
    }

    @Override
    public double getAngle() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), Branch.Side.ONE).otherSideA(parent, true);
    }

    @Override
    public double getP() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), Branch.Side.ONE).otherSideP(parent, true);
    }

    @Override
    public double getQ() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), Branch.Side.ONE).otherSideQ(parent, true);
    }

    @Override
    public Branch.Side getSide() {
        return null;
    }

    @Override
    public DanglingLine getConnectable() {
        return parent;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return parent.getTerminal().getVoltageLevel();
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
