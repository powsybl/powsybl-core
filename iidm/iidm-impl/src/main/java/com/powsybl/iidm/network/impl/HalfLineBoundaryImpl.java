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
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class HalfLineBoundaryImpl implements Boundary {

    private final Supplier<Terminal> terminalSupplier;

    private final TieLine parent;
    private final Branch.Side side;

    HalfLineBoundaryImpl(TieLine parent, Branch.Side side, Supplier<Terminal> terminalSupplier) {
        this.parent = Objects.requireNonNull(parent);
        this.side = Objects.requireNonNull(side);
        this.terminalSupplier = Objects.requireNonNull(terminalSupplier);
    }

    @Override
    public double getV() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideU(parent.getHalf(side));
    }

    @Override
    public double getAngle() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideA(parent.getHalf(side));
    }

    @Override
    public double getP() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideP(parent.getHalf(side));
    }

    @Override
    public double getQ() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideQ(parent.getHalf(side));
    }

    @Override
    public Branch.Side getSide() {
        return side;
    }

    @Override
    public Connectable getConnectable() {
        return parent;
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
