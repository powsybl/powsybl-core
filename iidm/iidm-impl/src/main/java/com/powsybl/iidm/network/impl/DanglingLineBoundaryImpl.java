/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DanglingLineBoundaryImpl implements Boundary {

    class DanglingLineBoundaryTerminalImpl implements BoundaryTerminal {

        @Override
        public VoltageLevel getVoltageLevel() {
            return parent.getTerminal().getVoltageLevel();
        }

        @Override
        public Connectable getConnectable() {
            return parent;
        }

        @Override
        public double getP() {
            return DanglingLineBoundaryImpl.this.getP();
        }

        @Override
        public double getQ() {
            return DanglingLineBoundaryImpl.this.getQ();
        }

        @Override
        public double getI() {
            return Math.hypot(getP(), getQ())
                    / (Math.sqrt(3.) * getV() / 1000);
        }

        @Override
        public boolean isConnected() {
            return false;
        }
    }

    private final DanglingLine parent;
    private final BoundaryTerminal terminal;

    DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
        terminal = new DanglingLineBoundaryTerminalImpl();
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

    @Override
    public BoundaryTerminal getTerminal() {
        return terminal;
    }

    private static double getV(Bus b) {
        return b == null ? Double.NaN : b.getV();
    }

    private static double getAngle(Bus b) {
        return b == null ? Double.NaN : b.getAngle();
    }
}
