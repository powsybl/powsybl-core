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

    private final TieLineImpl.HalfLineImpl parent;
    private final Branch.Side side;
    private final Branch.Side originalBoundarySide;

    HalfLineBoundaryImpl(TieLineImpl.HalfLineImpl parent, Branch.Side side, Branch.Side originalBoundarySide) {
        this.parent = Objects.requireNonNull(parent);
        this.side = Objects.requireNonNull(side);
        this.originalBoundarySide = Objects.requireNonNull(originalBoundarySide);
    }

    // side defines the side of the TieLine where we have to get the (S, V) values
    // otherSide(originalBoundarySide) defines the original side of the half line where the previous values are associated with.
    @Override
    public double getV() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), otherSide(originalBoundarySide)).otherSideU(parent);
    }

    @Override
    public double getAngle() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), otherSide(originalBoundarySide)).otherSideA(parent);
    }

    @Override
    public double getP() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), otherSide(originalBoundarySide)).otherSideP(parent);
    }

    @Override
    public double getQ() {
        Terminal t = getConnectable().getTerminal(side);
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b), otherSide(originalBoundarySide)).otherSideQ(parent);
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

    @Override
    public Branch.Side getOriginalBoundarySide() {
        return originalBoundarySide;
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }

    private static Branch.Side otherSide(Branch.Side side) {
        if (side == Branch.Side.ONE) {
            return Branch.Side.TWO;
        } else {
            return Branch.Side.ONE;
        }
    }
}
