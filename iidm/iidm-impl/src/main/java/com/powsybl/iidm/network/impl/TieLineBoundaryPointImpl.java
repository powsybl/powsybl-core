/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryPoint;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.util.SV;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TieLineBoundaryPointImpl implements BoundaryPoint {

    private final Supplier<Terminal> terminalGetter;

    private final TieLine.HalfLine halfLine;

    TieLineBoundaryPointImpl(TieLine.HalfLine halfLine, Supplier<Terminal> terminalGetter) {
        this.halfLine = Objects.requireNonNull(halfLine);
        this.terminalGetter = Objects.requireNonNull(terminalGetter);
    }

    @Override
    public double getV() {
        Terminal t = terminalGetter.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideU(halfLine);
    }

    @Override
    public double getAngle() {
        Terminal t = terminalGetter.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideA(halfLine);
    }

    @Override
    public double getP() {
        Terminal t = terminalGetter.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideP(halfLine);
    }

    @Override
    public double getQ() {
        Terminal t = terminalGetter.get();
        Bus b = t.getBusView().getBus();
        return new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSideQ(halfLine);
    }
}
