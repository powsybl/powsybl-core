/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class HalfLineBoundaryImpl implements Boundary {

    private final Supplier<Terminal> terminalSupplier;

    private final TieLine.HalfLine halfLine;

    HalfLineBoundaryImpl(TieLine.HalfLine halfLine, Supplier<Terminal> terminalSupplier) {
        this.halfLine = Objects.requireNonNull(halfLine);
        this.terminalSupplier = Objects.requireNonNull(terminalSupplier);
    }

    @Override
    public double getV() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideU(halfLine);
    }

    @Override
    public double getAngle() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideA(halfLine);
    }

    @Override
    public double getP() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideP(halfLine);
    }

    @Override
    public double getQ() {
        Terminal t = terminalSupplier.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), getV(b), getAngle(b)).otherSideQ(halfLine);
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
