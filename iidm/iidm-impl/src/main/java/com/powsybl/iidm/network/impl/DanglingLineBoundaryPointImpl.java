/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryPoint;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DanglingLineBoundaryPointImpl implements BoundaryPoint {

    private final DanglingLine parent;

    DanglingLineBoundaryPointImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideU(parent);
    }

    @Override
    public double getAngle() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideA(parent);
    }

    @Override
    public double getP() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideP(parent);
    }

    @Override
    public double getQ() {
        Terminal t = parent.getTerminal();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideQ(parent);
    }
}
