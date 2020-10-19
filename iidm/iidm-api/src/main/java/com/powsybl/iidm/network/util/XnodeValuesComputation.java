/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TieLine;

import java.util.function.Consumer;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class XnodeValuesComputation {

    public static void computeAndSetXnodeValues(TieLine.HalfLine halfLine, Terminal t, Consumer<SV> setter) {
        Bus b = t.getBusView().getBus();
        SV networkSV = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN);
        SV boundarySV = networkSV.otherSide(halfLine.getR(), halfLine.getX(), halfLine.getG1(), halfLine.getB1(), halfLine.getG2(), halfLine.getB2(), 1.0);
        setter.accept(boundarySV);
    }

    public static void computeAndSetXnodeValues(DanglingLine dl, Consumer<SV> setter) {
        Terminal t = dl.getTerminal();
        Bus b = t.getBusView().getBus();
        SV networkSV = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN);
        SV boundarySV = networkSV.otherSide(dl);
        setter.accept(boundarySV);
    }

    private XnodeValuesComputation() {
    }
}
