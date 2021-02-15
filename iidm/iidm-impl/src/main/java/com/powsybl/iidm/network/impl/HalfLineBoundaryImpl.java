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

    class HalfLineBoundaryTerminalImpl implements BoundaryTerminal {

        @Override
        public VoltageLevel getVoltageLevel() {
            return terminalSupplier.get().getVoltageLevel();
        }

        @Override
        public Connectable getConnectable() {
            return terminalSupplier.get().getConnectable();
        }

        @Override
        public double getP() {
            return HalfLineBoundaryImpl.this.getP();
        }

        @Override
        public double getQ() {
            return HalfLineBoundaryImpl.this.getQ();
        }

        @Override
        public double getI() {
            return Math.hypot(getP(), getQ())
                    / (Math.sqrt(3.) * getV() / 1000);
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }

    private final Supplier<Terminal> terminalSupplier;

    private final TieLine.HalfLine halfLine;
    private final BoundaryTerminal boundaryTerminal;

    HalfLineBoundaryImpl(TieLine.HalfLine halfLine, Supplier<Terminal> terminalSupplier) {
        this.halfLine = Objects.requireNonNull(halfLine);
        this.terminalSupplier = Objects.requireNonNull(terminalSupplier);
        boundaryTerminal = new HalfLineBoundaryTerminalImpl();
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

    @Override
    public BoundaryTerminal getTerminal() {
        return boundaryTerminal;
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
