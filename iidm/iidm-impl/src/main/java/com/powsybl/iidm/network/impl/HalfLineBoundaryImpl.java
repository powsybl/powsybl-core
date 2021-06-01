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
class HalfLineBoundaryImpl implements Boundary {
    // side represents the network side.
    // side here is Side.ONE for the half line 1 of a tie line.
    // side is Side.TWO for the half line 2 of a tie line.

    private final TieLineImpl.HalfLineImpl parent;
    private final Branch.Side side;

    HalfLineBoundaryImpl(TieLineImpl.HalfLineImpl parent, Branch.Side side) {
        this.parent = Objects.requireNonNull(parent);
        this.side = Objects.requireNonNull(side);
    }

    @Override
    public double getV() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), side).otherSideU(parent);
    }

    @Override
    public double getAngle() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), side).otherSideA(parent);
    }

    @Override
    public double getP() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), side).otherSideP(parent);
    }

    @Override
    public double getQ() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), side).otherSideQ(parent);
    }

    @Override
    public Branch.Side getSide() {
        return side;
    }

    @Override
    public TieLine getConnectable() {
        return parent.getParent();
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return getConnectable().getTerminal(side).getVoltageLevel();
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
